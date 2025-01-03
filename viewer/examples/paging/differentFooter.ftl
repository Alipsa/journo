<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="nl">
  <head>
    <style>
      @page {
        size: a5;
        margin: 210px 55px 120px 50px;
      }

      @page {
        @top-center {content: element(header);}
        @bottom-center {content: element(footer);}
      }
      
      @page lastPage {
        @top-center {content: element(header);}
        @bottom-center {content: element(footer);}
      }
      

      table {
        /* include thead and tfoot when page is breaking */
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

      /* overrride default footer with a named page */
      .lastpagefooter {
        page: lastPage;
        display: block; 
        position: running(footer);
        background-color: gray;
      }

    </style>
    </head>
    <body>
      <div class="header">
        <p class="pageheader">Page header</p>
      </div>
      <div class="footer">
        <p class="pagefooter">Page footer <br/> second line</p>
      </div>

      <table>
        <thead><tr><th>thead</th></tr></thead>
        <tbody><tr><td>001</td></tr>
        <tr><td>002</td></tr>
        <tr><td>003</td></tr>
        <tr><td>004</td></tr>
        <tr><td>005</td></tr>
        <tr><td>006</td></tr>
        <tr><td>007</td></tr>
        <tr><td>008</td></tr>
        <tr><td>009</td></tr>
        <tr><td>010</td></tr>
        <tr><td>011</td></tr>
        <tr><td>012</td></tr>
        <tr><td>013</td></tr>
        <tr><td>014</td></tr>
        <tr><td>015</td></tr>
        <tr><td>016</td></tr>
        <tr><td>017</td></tr>
        <tr><td>018</td></tr>
        <tr><td>019</td></tr>
        <tr><td>020</td></tr>
        <tr><td>021</td></tr>
        <tr><td>022</td></tr>
        <tr><td>023</td></tr>
        <tr><td>024</td></tr>
        <tr><td>025</td></tr></tbody>
        <tfoot><tr><th>tfoot</th></tr></tfoot>
      </table>
      <div class="footer">
        <p class="lastpagefooter">Last page footer</p>
      </div>
  </body>
</html>