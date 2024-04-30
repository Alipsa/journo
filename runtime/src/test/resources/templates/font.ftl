<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
    <title>Alice's Adventures in Wonderland -- Chapter I</title>
    <!-- there is no automatic handling of fonts in external css yet, so this will not work -->
    <link href="https://fonts.googleapis.com/css?family=Sofia"/>
    <style>
        @page {
            size: letter;
            margin: 0.25in;
            border: thin solid black;
            padding: 1em;
        }

        @font-face {
            font-family: "Jersey 25";
            src: url(${jerseyUrl});
            -fs-pdf-font-embed: embed;
        }

        @font-face {
            font-family: "Jacquard 24";
            src: url(${jacquardUrl});
            -fs-pdf-font-embed: embed;
        }

        body {
            font-family: "Courier New", monospace;
        }

        .sans {
            font-family: sans-serif;
        }

        .sofia {
            font-family: "Sofia", serif;
        }

        .jersey {
            /* The name of the font must match exactly to the font family property in the ttf
               hint: you can use the FontForge app to view the info */
            font-family: "Jersey 25", sans-serif;
        }
        .jacquard {
            font-family: "Jacquard 24", sans-serif;
        }
    </style>
</head>

<body>
<div class="content" id="page1">
    <h1>CHAPTER I</h1>

    <h2>Down the Rabbit-Hole</h2>

    <div class="sans">
        <p>SANS FONT</p>
        Alice was beginning to get very tired of
        sitting by her sister on the bank, and of having nothing to do: once or
        twice she had peeped into the book her sister was reading, but it had
        no pictures or conversations in it, `and what is the use of a book,'
        thought Alice `without pictures or conversation?'
    </div>
    <br />

    <div class="sofia">
        <p>SOFIA FONT</p>
        Alice was beginning to get very tired of
        sitting by her sister on the bank, and of having nothing to do: once or
        twice she had peeped into the book her sister was reading, but it had
        no pictures or conversations in it, `and what is the use of a book,'
        thought Alice `without pictures or conversation?'
    </div>
    <br />

    <div class="jersey">
        <p>JERSEY FONT</p>
        Alice was beginning to get very tired of
        sitting by her sister on the bank, and of having nothing to do: once or
        twice she had peeped into the book her sister was reading, but it had
        no pictures or conversations in it, `and what is the use of a book,'
        thought Alice `without pictures or conversation?'
    </div>
    <br />
    <div class="jacquard">
        <p>JACQUARD FONT</p>
        Alice was beginning to get very tired of
        sitting by her sister on the bank, and of having nothing to do: once or
        twice she had peeped into the book her sister was reading, but it had
        no pictures or conversations in it, `and what is the use of a book,'
        thought Alice `without pictures or conversation?'
    </div>
</div>
</body>
</html>