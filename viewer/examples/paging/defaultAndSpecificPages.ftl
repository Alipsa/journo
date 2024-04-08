<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
    <title>Alice's Adventures in Wonderland -- Chapter I</title>
    <style>

        @page firstPage {
            size: a5 portrait;
            margin: 20px;
            padding: 0;
            background-color: lightblue;
        }

        @page secondPage {
            size: a5 portrait;
            margin: 20px;
            padding: 10px;
            background-color: yellow;
            @bottom-right { content: element(footer) }
        }

        @page standardPage {
            size: a5 portrait;
            margin: 50px;
            padding: 0;
            @top-left { content: element(header) }
            @bottom-right { content: element(footer) }
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
            page: standardPage;
        }

        div.lastpage {
            page-break-after: avoid;
            margin: 0.25in;
            border: thin solid black;
            padding: 1em;
            page: standardPage;
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
<div id="page1">
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
Your override file should just re-assign values for properties originally given in xhtmlrenderer.conf. 
Please see the documentation in that file for a description of what each property affects.

<p>You can override either by dropping a configuration file in a specific location in your home directory, 
or by specifying an override file path using the -Dxr.conf=&lt;filename&gt; System property. 
If you specify the name of the override file on the command line, 
we do not look for an override file in your home directory.</p>
</div>
<div class="content">
  <p>
  The fictional character of Alice lives a posh lifestyle in the mid- to late 1800s in London, England. 
  She is highly intelligent, and like any well-raised girl, she is sophisticated and a great thinker for a seven-and-a-half-year-old child. 
  Alice is extremely brave, not being afraid to venture far out into new places or the unknown, 
  and she will become determined to investigate anything curious that makes her wonder.
  </p>
  <p>
  Alice does not have any friends, nor is she an outcast or loner. Much of her time is spent with family, 
  such as her older sister, who gives her daily lessons because Alice is homeschooled. 
  Outwardly, Alice is proper, well-behaved, well-groomed, and poised. 
  She has a charming elegance and grace beyond her years. She's a devoted lady, 
  always giving a polite curtsey when introducing herself. 
  </p>
</div>
<div class="content">
For a little girl, she is very well spoken, having a natural English accent. 
  She can memorize things very quickly and recite poetry and nursery rhymes with ease. 
  Alice knows all of the rules of a young lady's etiquette because she was brought up by a rather wealthy family from a privileged upper-class lifestyle. 
  It is quite likely that she is possibly even related to royalty through marriage or other aristocratic figures of high 
  society and respected positions during the time the stories take place.
</div>
<div class="lastpage">
    <h1>The last page</h1>
    <p class="figure">
        <img src="${imgAlice}" width="200px" height="300px" alt="rabbit"/>
        <br/>
        <b>White Rabbit checking watch</b>
    </p>
</div>
</body>
</html>