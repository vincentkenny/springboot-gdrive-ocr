package com.apply.sawit.controller;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

@Controller
public class GoogleDriveController {
    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE,
            "https://www.googleapis.com/auth/drive.install");

    private static final String USER_IDENTIFIER_KEY = "MY_DUMMY_USER";

    @Value("${google.oauth.callback.uri}")
    private String CALLBACK_URI;

    @Value("${google.secret.key.path}")
    private Resource gdSecretKeys;

    @Value("${google.credentials.folder.path}")
    private Resource credentialsFolder;

    @Value("${google.service.account.key}")
    private Resource serviceAccountKey;

    @Value("classpath:material/*")
    private Resource[] resources;

    @Value("${google.target.folder}")
    private String targetDriveFolder;

    private GoogleAuthorizationCodeFlow flow;

    @PostConstruct
    public void init() throws Exception {
        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(gdSecretKeys.getInputStream()));
        flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, secrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(credentialsFolder.getFile())).build();
    }

    @GetMapping(value = { "/upload" })
    public String showHomePage() throws Exception {
        boolean isUserAuthenticated = false;

        Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
        if (credential != null) {
            boolean tokenValid = credential.refreshToken();
            if (tokenValid) {
                isUserAuthenticated = true;
            }
        }

        return "upload.html";
    }

    @GetMapping(value = { "/googlesignin" })
    public void doGoogleSignIn(HttpServletResponse response) throws Exception {
        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        String redirectURL = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
        response.sendRedirect(redirectURL);
    }

    @GetMapping(value = { "/oauth" })
    public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
        String code = request.getParameter("code");
        if (code != null) {
            saveToken(code);
            createFile();

        }

        return "upload.html";
    }

    private void saveToken(String code) throws Exception {
        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
        flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);

    }

    @GetMapping(value = { "/create" })
    public void createFile() throws Exception {
        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);

        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName("springboot-sawitpro-test ").build();

        List<String> listOfFiles = getFilesInFolder(resources);
        ClassLoader classLoader = getClass().getClassLoader();
        for(String fileName : listOfFiles){
            File file = new File();
            file.setName(fileName);
            file.setParents(Arrays.asList(targetDriveFolder));
            FileContent content = new FileContent("image/jpeg", new java.io.File(classLoader.getResource("material/"+fileName).getFile()));
            File uploadedFile = drive.files().create(file, content).setFields("id").execute();
            System.out.println(String.format("{fileID: '%s'}", uploadedFile.getId()));
        }
    }

    public List<String> getFilesInFolder(Resource[] folderPath){
        List<String> fileNames = new ArrayList<>();
        for (Resource res : folderPath){
            fileNames.add(res.getFilename());
            System.out.println(res.getFilename());
        }

        return fileNames;
    }
}
