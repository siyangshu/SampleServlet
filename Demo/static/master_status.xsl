<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
  <html>
    <body>
      <p>Siyang Shu (shusiy)<p>
      <h2>Here are all the active workers:</h2>

      <table border="1">
  <tr bgcolor="#9acd32">
    <th>address</th>
    <th>status</th>
    <th>job</th>
    <th>keys read</th>
    <th>keys written</th>
  </tr>
  <xsl:for-each select="worker_collection/worker">
    <tr>
      <td><xsl:value-of select="ip" />:<xsl:value-of select="port" /></td>
      <td><xsl:value-of select="status" /></td>
      <td><xsl:value-of select="job" /></td>
      <td><xsl:value-of select="keys_read" /></td>
      <td><xsl:value-of select="keys_written" /></td>
    </tr>
  </xsl:for-each>
      </table>

      <h2>Submit your job:</h2>

      <form action="/job" method="post">
  job:<br />
  <input type="text" name="job" />
    <br />
      input directory:<br />
      <input type="text" name="input_directory" />
        <br />
    output directory:<br />
    <input type="text" name="output_directory" />
      <br />
        The number of map threads to run on each worker:<br />
        <input type="text" name="map_number" />
          <br />
      The number of reduce threads to run on each worker:<br />
      <input type="text" name="reduce_number" />
        <br />
          <input type="submit" value="Submit" />
          </form>

        </body>
      </html>
          </xsl:template>

        </xsl:stylesheet>
