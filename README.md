# springboot-gdrive-ocr

This is a experimental repo I used to learn Google Drive API and OCR using Springboot.

There are 2 controllers for each function:
- Google Drive Controller - To handle image uploads to a designated Google Drive folder,
- OCR Controller - To handle OCR of the images in english and chinese.

1. To use google drive API, first we have to create a credential to enable the google drive API via https://console.developers.google.com/ , add the libraries and follow the instruction provided

2. For the OCR, I use tesseract for java library to do the OCR and using the LSTM models from https://github.com/tesseract-ocr/tessdata_best

A screenshot of the Google Drive upload result:
<img width="907" alt="Screen Shot 2023-04-01 at 22 55 04" src="https://user-images.githubusercontent.com/48887892/229301150-33cec27b-66fe-45d7-8690-b1cf5cdf1f8f.png">

Screenshots for the OCR results for the english language model and chinese simplified language model:
<img width="1075" alt="Screen Shot 2023-04-01 at 22 56 48" src="https://user-images.githubusercontent.com/48887892/229301215-5af2b6a8-1e74-4a21-b733-be86777e3139.png">
English language model

<img width="752" alt="Screen Shot 2023-04-01 at 22 57 14" src="https://user-images.githubusercontent.com/48887892/229301232-52331e7c-da96-4dec-9389-7d7537531611.png">
Chinese simplified language model
