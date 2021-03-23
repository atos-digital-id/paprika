<?xml version="1.0" ?>
<!--

  Generate base code for pom comparing.

  Run:
  $ curl -s https://raw.githubusercontent.com/apache/maven/master/maven-model/src/main/mdo/maven.mdo | xsltproc brief.xslt - | xclip -selection clipboard

-->
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:m="http://codehaus-plexus.github.io/MODELLO/1.4.0" >

  <xsl:output method="text" />
  <xsl:variable name="vLower" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="vUpper" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

  <xsl:template match="/">
/*
 * How to generate Briefs classes:
 *  * look at brief.xslt, run it and paste the result here.
 *  * Add package and import lines
 *  * Change BriefModel visibility
 *  * Change all "fake" boolean String to boolean and change getters.
 *  * Remove all unused code.
 *  * Remove all "internal utility" fields.
 *  * Remove all deprecated fields and classes.
 *  * Remove all unwanted fields:
 *    * ModelBase.modules
 */
public class Briefs {

    <xsl:for-each select="m:model/m:classes/m:class">
      @Data
      <xsl:if test="./m:superClass" >@EqualsAndHashCode( callSuper = true )
      </xsl:if>private static class Brief<xsl:value-of select="m:name"/>
      <xsl:if test="./m:superClass" > extends Brief<xsl:value-of select="m:superClass"/></xsl:if> {
      <xsl:for-each select="m:fields/m:field">
        <xsl:variable name="type" select="m:type|m:association/m:type"/>
        <xsl:variable name="brief_type">
          <xsl:choose>
            <xsl:when test="$type = 'boolean'">Boolean</xsl:when>
            <xsl:when test="$type = 'int'">Integer</xsl:when>
            <xsl:when test="$type = 'String'">String</xsl:when>
            <xsl:when test="$type = 'DOM'">Object</xsl:when>
            <xsl:otherwise>Brief<xsl:value-of select="$type"/></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$type = 'Properties'">
        private final Properties <xsl:value-of select="m:name"/>;</xsl:when>
          <xsl:when test="m:association/m:multiplicity = '*'">
        private final Set&lt;<xsl:value-of select="$brief_type"/>&gt; <xsl:value-of select="m:name"/>;</xsl:when>
          <xsl:otherwise>
        private final <xsl:value-of select="$brief_type"/><xsl:text> </xsl:text><xsl:value-of select="m:name"/>;</xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>

        private Brief<xsl:value-of select="m:name"/>( <xsl:value-of select="m:name"/> model ) {<xsl:if test="./m:superClass" >
          super( model );</xsl:if><xsl:for-each select="m:fields/m:field">
            <xsl:variable name="type" select="m:type|m:association/m:type"/>
            <xsl:variable name="get">model.<xsl:choose><xsl:when test="$type= 'boolean'">is</xsl:when><xsl:otherwise>get</xsl:otherwise></xsl:choose><xsl:value-of select="concat(translate(substring(m:name,1,1), $vLower, $vUpper), substring(m:name, 2))"/>()</xsl:variable>
            <xsl:variable name="convert">
              <xsl:choose>
                <xsl:when test="$type = 'Properties'"><xsl:value-of select="$get"/></xsl:when>
                <xsl:when test="$type = 'boolean' or $type = 'int' or $type = 'String' or $type = 'DOM'">
                  <xsl:choose>
                    <xsl:when test="m:association/m:multiplicity = '*'"><xsl:value-of select="$get"/> == null ? null : new HashSet&lt;&gt;( <xsl:value-of select="$get"/> )</xsl:when>
                    <xsl:otherwise><xsl:value-of select="$get"/></xsl:otherwise>
                  </xsl:choose>
                </xsl:when>
                <xsl:otherwise>Brief<xsl:value-of select="$type"/>.of<xsl:value-of select="$type"/>( <xsl:value-of select="$get"/> )</xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
          this.<xsl:value-of select="m:name"/> = <xsl:value-of select="$convert"/>;</xsl:for-each>
        }

        public static Brief<xsl:value-of select="m:name"/> of<xsl:value-of select="m:name"/>( <xsl:value-of select="m:name"/> model ) {
          return model == null ? null : new Brief<xsl:value-of select="m:name"/>( model );
        }

        public static Set&lt;Brief<xsl:value-of select="m:name"/>&gt; of<xsl:value-of select="m:name"/>( Collection&lt;<xsl:value-of select="m:name"/>&gt; coll ) {
          if( coll == null )
            return null;
          Set&lt;Brief<xsl:value-of select="m:name"/>&gt; set = new HashSet&lt;&gt;();
          for( <xsl:value-of select="m:name"/> model: coll )
            set.add( of<xsl:value-of select="m:name"/>( model ) );
          return set;
        }

      }

    </xsl:for-each>
}
  </xsl:template>

</xsl:stylesheet>
