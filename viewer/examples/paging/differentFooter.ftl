<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="nl">
  <head>
    <style>
      @page {
        size: 793px 1122px;
        margin: 210px 55px 120px 50px;
      }

      @page {
        @top-center {content: element(header);}
        @bottom-center {content: element(footer) element(lastfooter);}
      }

      table {
        -fs-table-paginate: paginate;
      }

      .pageheader {
        display: block; 
        position: running(header);
        background-color: #ff0000;
      }

      .pagefooter {
        display: block; 
        position: running(footer);
        background-color: #00ff00;
      }

      .lastpagefooter {
        display: block; 
        position: running(lastfooter);
        margin-top: -200px;
      }

      .lastpagefootermargin {
        height: 200px;
        background-color: #0000ff;
      }
    </style>
    </head>
    <body>
      <div class="header">
        <p class="pageheader">Page header</p>
      </div>
      <div class="footer">
        <p class="pagefooter">Page footer</p>
      </div>
      <table>
        <thead><tr><th>thead</th></tr></thead>
        <tbody><tr><td>001</td></tr></tbody>
        <tbody><tr><td>002</td></tr></tbody>
        <tbody><tr><td>003</td></tr></tbody>
        <tbody><tr><td>004</td></tr></tbody>
        <tbody><tr><td>005</td></tr></tbody>
        <tbody><tr><td>006</td></tr></tbody>
        <tbody><tr><td>007</td></tr></tbody>
        <tbody><tr><td>008</td></tr></tbody>
        <tbody><tr><td>009</td></tr></tbody>
        <tbody><tr><td>010</td></tr></tbody>
        <tbody><tr><td>011</td></tr></tbody>
        <tbody><tr><td>012</td></tr></tbody>
        <tbody><tr><td>013</td></tr></tbody>
        <tbody><tr><td>014</td></tr></tbody>
        <tbody><tr><td>015</td></tr></tbody>
        <tbody><tr><td>016</td></tr></tbody>
        <tbody><tr><td>017</td></tr></tbody>
        <tbody><tr><td>018</td></tr></tbody>
        <tbody><tr><td>019</td></tr></tbody>
        <tbody><tr><td>020</td></tr></tbody>
        <tbody><tr><td>021</td></tr></tbody>
        <tbody><tr><td>022</td></tr></tbody>
        <tbody><tr><td>023</td></tr></tbody>
        <tbody><tr><td>024</td></tr></tbody>
        <tbody><tr><td>025</td></tr></tbody>
        <tfoot><tr><th>tfoot</th></tr></tfoot>
      </table>
      <div class="lastpagefootermargin">&#160;</div>
      <div class="lastpagefooter">Added to last page footer</div>
  </body>
</html>