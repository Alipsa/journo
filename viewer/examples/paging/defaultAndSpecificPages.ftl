<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
    <title>Alice's Adventures in Wonderland -- Chapter I</title>
    <style>

        @page firstPage {
            size: 4.18in 6.88in;
            margin: 20px;
            padding: 0;
            background-color: lightblue;
        }

        @page secondPage {
            size: 4.18in 6.88in;
            margin: 20px;
            padding: 10px;
            background-color: yellow;
        }

        @page {
            size: 4.18in 6.88in;
            margin: 20px;
            padding: 0;
        }

        div.header {
            font-size: 9px;
            font-style: italic;
            font-family: serif;
            text-align: left;
            display: block;
            position: running(header);
            background: inherit;
        }

        div.footer {
            font-size: 90%;
            font-style: italic;
            text-align: right;
            display: block;
            position: running(footer);
            background: inherit;
        }

        div.content {
            page-break-after: always;
            margin: 0.25in;
            border: thin solid black;
            padding: 1em;
        }
        div.lastpage {page-break-after: avoid;}

        @page {
            @top-left { content: element(header) }
        }
        @page {
            @bottom-right { content: element(footer) }
        }

        #pagenumber:before {
            content: counter(page);
        }

        #pagecount:before {
            content: counter(pages);
        }

        .dropcap {
            float: left;
            line-height: 80%;
            width: .7em;
            font-size: 400%;
        }
        #page1 {
            page: firstPage;
        }

        #page2 {
            page: secondPage;
        }

        /* avoid an extra blank page to be inserted on named pages */
        body {
            margin-top: 0;
        }
    </style>
</head>

<body>
<div class="header">Alice's Adventures in Wonderland</div>
<div class="footer" style="">  Page <span id="pagenumber"/> of <span id="pagecount"/> </div>
<div class="content" id="page1">
    <h1>CHAPTER I</h1>

    <h2>Down the Rabbit-Hole</h2>

    <p>
        <span class="dropcap">A</span>lice was beginning to get very tired of
        sitting by her sister on the bank, and of having nothing to do: once or
        twice she had peeped into the book her sister was reading, but it had
        no pictures or conversations in it, `and what is the use of a book,'
        thought Alice `without pictures or conversation?'
    </p>

    <p>So she was considering in her own mind (as well as she could,
        for the hot day made her feel very sleepy and stupid), whether the
        pleasure of making a daisy-chain would be worth the trouble of
        getting up and picking the daisies, when suddenly a White Rabbit
        with pink eyes ran close by her. </p>
</div>
<div id="page2">
    <div class="content" >
        Your override file should just re-assign values for properties originally given in xhtmlrenderer.conf. Please see the documentation in that file for a description of what each property affects.

        You can override either by dropping a configuration file in a specific location in your home directory, or by specifying an override file path using the -Dxr.conf=<filename> System property. If you specify the name of the override file on the command line, we do not look for an override file in your home directory.
    </div>
</div>
<div class="lastpage" id="page3">
    <p class="figure">
        <img src="${imgAlice}" width="200px" height="300px" alt="rabbit"/>
        <br/>
        <b>White Rabbit checking watch</b>
    </p>
</div>
</body>
</html>