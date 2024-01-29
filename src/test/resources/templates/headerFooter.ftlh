<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
    <title>Alice's Adventures in Wonderland -- Chapter I</title>
    <style>
        @page {
            size: 4.18in 6.88in;
            margin: 0.25in;
            border: thin solid black;
            padding: 1em;
        }

        div.header {
            font-size: 9px;
            font-style: italic;
            font-family: serif;
            text-align: right;
            display: block;
            position: running(header);
        }

        div.footer {
            font-size: 90%;
            font-style: italic;
            text-align: right;
            display: block;
            position: running(footer);
        }

        div.content {page-break-after: always;}
        div.lastpage {page-break-after: avoid;}

        @page {
            @top-right { content: element(header) }
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
<div class="lastpage" id="page2">
    <p class="figure">
        <img src="${alice2}" width="200px" height="300px" alt=""/>
        <br/>
        <b>White Rabbit checking watch</b>
    </p>
</div>
</body>
</html>