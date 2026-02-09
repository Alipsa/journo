<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE html>
<html>
<head>
  <title>PDF Report</title>
  <meta charset="UTF-8"></meta>
  <style type='text/css'> 
    @font-face {
      font-family: 'DejaVu Sans';
      src: url(https://www.1001fonts.com/download/font/dejavu-sans.book.ttf);
      -fs-pdf-font-embed: embed;
    }
    
    body {
      font-family: 'DejaVu Sans';
    }        
  </style>
</head>
<body>
<h1>${title}</h1>
<ol>
  <li>&#8377;${content}</li> 
  <li>&#x20B9;${content}</li>
  <li>₹${content}</li>
</ol>
<span> Full name: 朱凡鲁 </span>
</body>
</html>