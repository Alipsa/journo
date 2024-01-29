<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
    <style>
        svg {display: block;width:400px;height:400px}
    </style>
</head>
<body>
<h1>A big SVG circle</h1>
<div>
    <svg xmlns="http://www.w3.org/2000/svg">
        <circle cx="150" cy="65" r="60" stroke="black" stroke-width="3"
                fill="red" />
    </svg>
    <#noautoesc>
    ${svgImage}
    </#noautoesc>
</div>
</body>
</html>