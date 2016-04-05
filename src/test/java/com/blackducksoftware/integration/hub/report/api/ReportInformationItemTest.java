package com.blackducksoftware.integration.hub.report.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.joda.time.DateTime;
import org.junit.Test;

import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;

public class ReportInformationItemTest {

	@Test
	public void testReportMetaInformationItem() {
		final String reportFormat1 = "format1";
		final String locale1 = "local1";
		final String fileName1 = "file1";
		final int fileSize1 = 1;
		final String createdAt1 = null;
		final String updatedAt1 = null;
		final String finishedAt1 = null;
		final String createdBy1 = "user1";
		final List<MetaLink> links1 = new ArrayList<MetaLink>();
		final String href1 = "href1";
		final String rel1 = "rel1";
		final MetaLink metaLink1 = new MetaLink(rel1, href1);
		links1.add(metaLink1);
		final List<String> allow1 = new ArrayList<String>();
		allow1.add("GET");
		final MetaInformation _meta1 = new MetaInformation(allow1, href1, links1);

		final DateTime date = new DateTime();

		final String reportFormat2 = "format2";
		final String locale2 = "local2";
		final String fileName2 = "file2";
		final int fileSize2 = 2;
		final String createdAt2 = date.toString();
		final String updatedAt2 = date.toString();
		final String finishedAt2 = date.toString();
		final String createdBy2 = "user2";
		final List<MetaLink> links2 = new ArrayList<MetaLink>();
		final String href2 = "href2";
		final String rel2 = "rel2";
		final MetaLink metaLink2 = new MetaLink(rel2, href2);
		links2.add(metaLink2);
		final List<String> allow2 = new ArrayList<String>();
		final MetaInformation _meta2 = new MetaInformation(allow2, href2, links2);

		final MetaInformation _meta3 = new MetaInformation(allow1, href1, links1);
		final MetaLink metaLink3 = new MetaLink(rel1, href1);

		final ReportInformationItem item1 = new ReportInformationItem(reportFormat1, locale1,
				fileName1, fileSize1, createdAt1, updatedAt1, finishedAt1, createdBy1, _meta1);
		final ReportInformationItem item2 = new ReportInformationItem(reportFormat2, locale2,
				fileName2, fileSize2, createdAt2, updatedAt2, finishedAt2, createdBy2, _meta2);
		final ReportInformationItem item3 = new ReportInformationItem(reportFormat1, locale1,
				fileName1, fileSize1, createdAt1, updatedAt1, finishedAt1, createdBy1, _meta1);

		assertEquals(reportFormat1, item1.getReportFormat());
		assertEquals(locale1, item1.getLocale());
		assertEquals(fileName1, item1.getFileName());
		assertEquals(fileSize1, item1.getFileSize());
		assertEquals(createdAt1, item1.getCreatedAt());
		assertEquals(updatedAt1, item1.getUpdatedAt());
		assertEquals(finishedAt1, item1.getFinishedAt());
		assertEquals(createdBy1, item1.getCreatedBy());
		assertEquals(_meta1, item1.get_meta());

		assertEquals(reportFormat2, item2.getReportFormat());
		assertEquals(locale2, item2.getLocale());
		assertEquals(fileName2, item2.getFileName());
		assertEquals(fileSize2, item2.getFileSize());
		assertEquals(createdAt2, item2.getCreatedAt());
		assertEquals(updatedAt2, item2.getUpdatedAt());
		assertEquals(finishedAt2, item2.getFinishedAt());
		assertEquals(createdBy2, item2.getCreatedBy());
		assertEquals(_meta2, item2.get_meta());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		EqualsVerifier.forClass(ReportInformationItem.class).suppress(Warning.STRICT_INHERITANCE).verify();
		EqualsVerifier.forClass(MetaInformation.class).suppress(Warning.STRICT_INHERITANCE).verify();
		EqualsVerifier.forClass(MetaLink.class).suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		assertNull(item1.getTimeCreatedAt());
		assertNull(item1.getTimeUpdatedAt());
		assertNull(item1.getTimeFinishedAt());

		assertEquals(date, item2.getTimeCreatedAt());
		assertEquals(date, item2.getTimeUpdatedAt());
		assertEquals(date, item2.getTimeFinishedAt());

		assertEquals(href1, _meta1.getHref());
		assertEquals(links1, _meta1.getLinks());
		assertEquals(allow1, _meta1.getAllow());

		assertEquals(href2, _meta2.getHref());
		assertEquals(links2, _meta2.getLinks());
		assertEquals(allow2, _meta2.getAllow());

		assertTrue(!_meta1.equals(_meta2));
		assertTrue(_meta1.equals(_meta3));

		assertTrue(_meta1.hashCode() != _meta2.hashCode());
		assertEquals(_meta1.hashCode(), _meta3.hashCode());

		assertEquals(href1, metaLink1.getHref());
		assertEquals(rel1, metaLink1.getRel());

		assertEquals(href2, metaLink2.getHref());
		assertEquals(rel2, metaLink2.getRel());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		assertTrue(metaLink1.hashCode() != metaLink2.hashCode());
		assertEquals(metaLink1.hashCode(), metaLink3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("ReportMetaInformationItem [reportFormat=");
		builder.append(item1.getReportFormat());
		builder.append(", locale=");
		builder.append(item1.getLocale());
		builder.append(", fileName=");
		builder.append(item1.getFileName());
		builder.append(", fileSize=");
		builder.append(item1.getFileSize());
		builder.append(", createdAt=");
		builder.append(item1.getCreatedAt());
		builder.append(", updatedAt=");
		builder.append(item1.getUpdatedAt());
		builder.append(", finishedAt=");
		builder.append(item1.getFinishedAt());
		builder.append(", createdBy=");
		builder.append(item1.getCreatedBy());
		builder.append(", _meta=");
		builder.append(item1.get_meta());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());

	}
}
