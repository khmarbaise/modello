package org.codehaus.modello.generator.xml.stax;

/*
 * Copyright (c) 2004, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import junit.framework.Assert;
import org.codehaus.modello.test.model.Build;
import org.codehaus.modello.test.model.Component;
import org.codehaus.modello.test.model.MailingList;
import org.codehaus.modello.test.model.Model;
import org.codehaus.modello.test.model.Organization;
import org.codehaus.modello.test.model.Repository;
import org.codehaus.modello.test.model.Scm;
import org.codehaus.modello.test.model.SourceModification;
import org.codehaus.modello.test.model.io.stax.MavenStaxReader;
import org.codehaus.modello.test.model.io.stax.MavenStaxWriter;
import org.codehaus.modello.test.model.TypeTester;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import javax.xml.stream.XMLStreamException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: Xpp3Verifier.java 675 2006-11-16 10:58:59Z brett $
 */
public class StaxVerifier
    extends Verifier
{
    /**
     * TODO: Add a association thats not under the root element
     */
    public void verify()
        throws IOException, XMLStreamException
    {
        TimeZone.setDefault( TimeZone.getTimeZone( "America/New_York" ) );

        verifyWriter();

        verifyReader();

        verifyReaderAliases();

        verifyReaderDuplicates();
    }

    public void verifyEncodedRead()
        throws IOException, XMLStreamException
    {
        File file = new File( "src/test/verifiers/stax/expected-encoding.xml" );

        Reader reader = ReaderFactory.newXmlReader( file );
        MavenStaxReader modelReader = new MavenStaxReader();

        Model model = modelReader.read( reader );

        Assert.assertEquals( "Maven\u00A9", model.getName() );
    }

    public void verifyReadDefaultAttribute()
        throws IOException, XMLStreamException
    {
        File file = new File( "src/test/verifiers/stax/expected-default-extend.xml" );

        Reader reader = ReaderFactory.newXmlReader( file );
        MavenStaxReader modelReader = new MavenStaxReader();

        Model model = modelReader.read( reader );

        Assert.assertEquals( "foo", model.getExtend() );
    }

    public void verifyWriter()
        throws IOException, XMLStreamException
    {
        String expectedXml = FileUtils.fileRead( getTestFile( "src/test/verifiers/stax/expected.xml" ) );

        // ----------------------------------------------------------------------
        // Build the model thats going to be written.
        // ----------------------------------------------------------------------

        Model expected = new Model();

        expected.setExtend( "/foo/bar" );

        expected.setName( "Maven" );

        expected.setModelVersion( "4.0.0" );

        MailingList mailingList = new MailingList();

        mailingList.setName( "Mailing list" );

        mailingList.setSubscribe( "Super Subscribe" );

        mailingList.setUnsubscribe( "Duper Unsubscribe" );

        mailingList.setArchive( "?ber Archive" );

        expected.addMailingList( mailingList );

        Scm scm = new Scm();

        String connection = "connection";

        String developerConnection = "developerConnection";

        String url = "url";

        scm.setConnection( connection );

        scm.setDeveloperConnection( developerConnection );

        scm.setUrl( url );

        expected.setScm( scm );

        Build build = new Build();

        build.setSourceDirectory( "src/main/java" );

        build.setUnitTestSourceDirectory( "src/test/java" );

        SourceModification sourceModification = new SourceModification();

        sourceModification.setClassName( "excludeEclipsePlugin" );

        sourceModification.setDirectory( "foo" );

        sourceModification.addExclude( "de/abstrakt/tools/codegeneration/eclipse/*.java" );

        build.addSourceModification( sourceModification );

        expected.setBuild( build );

        Component component = new Component();

        component.setName( "component1" );

        expected.addComponent( component );

        component = new Component();

        component.setName( "component2" );

        component.setComment( "comment2" );

        expected.addComponent( component );

        Component c2 = new Component();

        c2.setName( "sub" );

        c2.setComment( "subcomment" );

        component.getComponents().add( c2 );

        component = new Component();

        component.setName( "component3" );

        Xpp3Dom xpp3Dom = new Xpp3Dom( "custom" );
        Xpp3Dom child = new Xpp3Dom( "foo" );
        child.setValue( "bar" );
        xpp3Dom.addChild( child );
        child = new Xpp3Dom( "bar" );
        child.setAttribute( "att1", "value" );
        child.setValue( "baz" );
        xpp3Dom.addChild( child );
        child = new Xpp3Dom( "el1" );
        xpp3Dom.addChild( child );
        Xpp3Dom el1 = child;
        child = new Xpp3Dom( "el2" );
        child.setValue( "te&xt" );
        el1.addChild( child );

        component.setCustom( xpp3Dom );

        expected.addComponent( component );

        component = new Component();
        component.setName( "component4" );
        expected.addComponent( component );

        Properties properties = new Properties();
        properties.setProperty( "name", "value" );
        component.setFlatProperties( properties );

        properties = new Properties();
        properties.setProperty( "key", "theValue" );
        component.setProperties( properties );

        TypeTester typeTester = new TypeTester();
        typeTester.setC( 'v' );
        typeTester.setI( 1 );
        typeTester.setS( (short) 2 );
        typeTester.setL( 3L );
        typeTester.setF( 4.5f );
        typeTester.setD( 5.6 );
        try
        {
            typeTester.setDate( new java.util.Date( 1136551996L ) );
            typeTester.setFormattedDate( new java.text.SimpleDateFormat( "yyyyMMdd.hhmmss" ).parse( "20061112.123456" ) );
        }
        catch ( java.text.ParseException e )
        {
            throw new XMLStreamException( "Couldn't set date: " + e.getMessage() );
        }

        expected.setTypeTester( typeTester );

        Repository repository = new Repository();
        repository.setId( "foo" );
        expected.addRepository( repository );

        repository = new Repository();
        repository.setId( "bar" );
        expected.addRepository( repository );

        // ----------------------------------------------------------------------
        // Write out the model
        // ----------------------------------------------------------------------

        MavenStaxWriter writer = new MavenStaxWriter();

        StringWriter buffer = new StringWriter();

        writer.write( buffer, expected );

        String actualXml = buffer.toString();

//      System.out.println( expectedXml );
//
//        System.out.println( actualXml );

        Assert.assertEquals( cleanLineEndings( expectedXml.trim() ), scrubXmlDeclQuotes( actualXml.trim() ) );

        MavenStaxReader reader = new MavenStaxReader();

        Model actual = reader.read( new StringReader( actualXml ) );

        Assert.assertNotNull( "Actual", actual );

        assertModel( expected, actual );

        buffer = new StringWriter();

        writer.write( buffer, actual );

        Assert.assertEquals( cleanLineEndings( expectedXml.trim() ), scrubXmlDeclQuotes( buffer.toString().trim() ) );
    }

    private String cleanLineEndings( String s )
    {
        return s.replaceAll( "\r\n", "\n" );
    }

    private String scrubXmlDeclQuotes( String s )
    {
        s = cleanLineEndings( s );

        if ( s.startsWith( "<?xml version='1.0' encoding='UTF-8'?>"))
        {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + s.substring( "<?xml version='1.0' encoding='UTF-8'?>".length() );
        }
        return s;
    }

    public void verifyReader()
        throws IOException, XMLStreamException
    {
        MavenStaxReader reader = new MavenStaxReader();

        // ----------------------------------------------------------------------
        // Test that the entities is properly resolved
        // ----------------------------------------------------------------------

        String xml = "<mavenModel>\n" + "  <groupId>Laugst\u00f8l</groupId>\n" + "</mavenModel>";

        Model expected = new Model();

        String groupId = "Laugst\u00f8l";

        expected.setGroupId( groupId );

        Model actual = reader.read( new StringReader( xml ) );

        assertModel( expected, actual );
    }

    public void verifyReaderAliases()
        throws IOException, XMLStreamException
    {
        MavenStaxReader reader = new MavenStaxReader();

        String xml = "<mavenModel>\n" + "  <website>http://maven.apache.org/website</website>\n" +
            "  <organisation><name>my-org</name></organisation>\n" + "</mavenModel>";

        Model expected = new Model();

        expected.setUrl( "http://maven.apache.org/website" );

        Organization org = new Organization();

        org.setName( "my-org" );

        expected.setOrganization( org );

        Model actual = reader.read( new StringReader( xml ) );

        assertModel( expected, actual );
    }

    public void verifyReaderDuplicates()
        throws IOException, XMLStreamException
    {
        MavenStaxReader reader = new MavenStaxReader();

        String xml =
            "<mavenModel>\n" + "  <builder><sourceDirectory /><sourceDirectory /></builder>\n" + "</mavenModel>";

        try
        {
            reader.read( new StringReader( xml ) );
            Assert.fail( "Should have obtained a parse error for duplicate sourceDirectory" );
        }
        catch ( XMLStreamException expected )
        {
            Assert.assertTrue( true );
        }

        xml = "<mavenModel>\n" + "  <builder><sourceDirectory /></builder>\n" +
            "  <builder><sourceDirectory /></builder>\n" + "</mavenModel>";

        try
        {
            reader.read( new StringReader( xml ) );
            Assert.fail( "Should have obtained a parse error for duplicate build" );
        }
        catch ( XMLStreamException expected )
        {
            Assert.assertTrue( true );
        }
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

    public void assertModel( Model expected, Model actual )
    {
        Assert.assertNotNull( "Actual model", actual );

        Assert.assertEquals( "/model/extend", expected.getExtend(), actual.getExtend() );

//        assertParent( expected.getParent(), actual.getParent() );

        Assert.assertEquals( "/model/modelVersion", expected.getModelVersion(), actual.getModelVersion() );

        Assert.assertEquals( "/model/groupId", expected.getGroupId(), actual.getGroupId() );

        Assert.assertEquals( "/model/artifactId", expected.getArtifactId(), actual.getArtifactId() );

        Assert.assertEquals( "/model/type", expected.getType(), actual.getType() );

        Assert.assertEquals( "/model/name", expected.getName(), actual.getName() );

        Assert.assertEquals( "/model/version", expected.getVersion(), actual.getVersion() );

        Assert.assertEquals( "/model/shortDescription", expected.getShortDescription(), actual.getShortDescription() );

        Assert.assertEquals( "/model/description", expected.getDescription(), actual.getDescription() );

        Assert.assertEquals( "/model/url", expected.getUrl(), actual.getUrl() );

        Assert.assertEquals( "/model/logo", expected.getLogo(), actual.getLogo() );

//        assertIssueManagement();

//        assertCiManagement();

        Assert.assertEquals( "/model/inceptionYear", expected.getInceptionYear(), actual.getInceptionYear() );

//        assertEquals( "/model/siteAddress", expected.getSiteAddress(), actual.getSiteAddress() );

//        assertEquals( "/model/siteDirectory", expected.getSiteDirectory(), actual.getSiteDirectory() );

//        assertEquals( "/model/distributionSite", expected.getDistributionSite(), actual.getDistributionSite() );

//        assertEquals( "/model/distributionDirectory", expected.getDistributionDirectory(), actual.getDistributionDirectory() );

        assertMailingLists( expected.getMailingLists(), actual.getMailingLists() );
/*
        assertDevelopers( );

        assertContributors( );

        assertDependencies( );

        assertLicenses( );

        assertPackageGroups( );

        assertReports( );
*/
        assertScm( expected.getScm(), actual.getScm() );
/*
        assertBuild( );

        assertOrganization( expected.getOrganization(), actual.getOrganization() );
*/
        assertBuild( expected.getBuild(), actual.getBuild() );
    }

    public void assertMailingLists( List expected, List actual )
    {
        Assert.assertNotNull( "/model/mailingLists", actual );

        Assert.assertEquals( "/model/mailingLists.size", expected.size(), actual.size() );

        for ( int i = 0; i < expected.size(); i++ )
        {
            assertMailingList( i, (MailingList) expected.get( i ), actual.get( i ) );
        }
    }

    public void assertMailingList( int i, MailingList expected, Object actualObject )
    {
        Assert.assertNotNull( "/model/mailingLists[" + i + "]", actualObject );

        Assert.assertEquals( "/model/mailingLists", MailingList.class, actualObject.getClass() );

        MailingList actual = (MailingList) actualObject;

        Assert.assertEquals( "/model/mailingLists[" + i + "]/name", expected.getName(), actual.getName() );

        Assert.assertEquals( "/model/mailingLists[" + i + "]/subscribe", expected.getSubscribe(),
                             actual.getSubscribe() );

        Assert.assertEquals( "/model/mailingLists[" + i + "]/unsubscribe", expected.getUnsubscribe(),
                             actual.getUnsubscribe() );

        Assert.assertEquals( "/model/mailingLists[" + i + "]/archive", expected.getArchive(), actual.getArchive() );
    }

    public void assertScm( Scm expected, Object actualObject )
    {
        if ( expected == null )
        {
            Assert.assertNull( "/model/scm", actualObject );
        }
        else
        {
            Assert.assertNotNull( "/model/scm", actualObject );

            Assert.assertEquals( "/model/scm", Scm.class, actualObject.getClass() );

            Scm actual = (Scm) actualObject;

            Assert.assertEquals( "/model/scm/connection", expected.getConnection(), actual.getConnection() );

            Assert.assertEquals( "/model/scm/developerConnection", expected.getDeveloperConnection(),
                                 actual.getDeveloperConnection() );

            Assert.assertEquals( "/model/scm/url", expected.getUrl(), actual.getUrl() );
        }
    }

    public void assertBuild( Build expected, Object actualObject )
    {
        if ( expected == null )
        {
            Assert.assertNull( "/model/builder", actualObject );
        }
        else
        {
            Assert.assertNotNull( "/model/builder", actualObject );

            Assert.assertEquals( "/model/builder", Build.class, actualObject.getClass() );

            Build actual = (Build) actualObject;

            Assert.assertEquals( "/model/builder/sourceDirectory", expected.getSourceDirectory(),
                                 actual.getSourceDirectory() );

            Assert.assertEquals( "/model/builder/unitTestSourceDirectory", expected.getUnitTestSourceDirectory(),
                                 actual.getUnitTestSourceDirectory() );
        }
    }
}
