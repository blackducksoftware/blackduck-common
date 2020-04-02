package com.synopsys.integration.blackduck.dataservice.report

import com.synopsys.integration.blackduck.TimingExtension
import com.synopsys.integration.blackduck.api.generated.enumeration.LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.enumeration.ProjectVersionPhaseType
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView
import com.synopsys.integration.blackduck.api.generated.view.ProjectView
import com.synopsys.integration.blackduck.rest.IntHttpClientTestHelper
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory
import com.synopsys.integration.blackduck.service.ProjectService
import com.synopsys.integration.blackduck.service.ReportService
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.TempDirectory

import java.nio.file.Path

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

@Tag("integration")
@ExtendWith(TimingExtension.class)
class RiskReportServiceTestIT {
    private static final IntHttpClientTestHelper restConnectionTestHelper = new IntHttpClientTestHelper()

    @BeforeAll
    public static void createProjectFirst() {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT")
        final String testProjectVersionName = restConnectionTestHelper.getProperty("TEST_VERSION")
        final String testPhase = restConnectionTestHelper.getProperty("TEST_PHASE")
        final String testDistribution = restConnectionTestHelper.getProperty("TEST_DISTRIBUTION")

        ProjectSyncModel projectSyncModel = new ProjectSyncModel(testProjectName, testProjectVersionName);
        projectSyncModel.setPhase(ProjectVersionPhaseType.valueOf(testPhase));
        projectSyncModel.setDistribution(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.valueOf(testDistribution));

        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory()
        final ProjectService projectService = blackDuckServicesFactory.createProjectService();

        projectService.syncProjectAndVersion(projectSyncModel)
    }

    @Test
    @ExtendWith(TempDirectory.class)
    public void createReportPdfFileTest(@TempDirectory.TempDir Path folderForReport) {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT")
        final String testProjectVersionName = restConnectionTestHelper.getProperty("TEST_VERSION")

        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory()
        ProjectService projectService = blackDuckServicesFactory.createProjectService()
        Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion(testProjectName, testProjectVersionName)
        ReportService riskReportService = blackDuckServicesFactory.createReportService(30000)
        ProjectView projectView = projectVersionWrapper.get().getProjectView()
        ProjectVersionView projectVersionView = projectVersionWrapper.get().getProjectVersionView()

        File folder = folderForReport.toFile();
        File pdfFile = riskReportService.createReportPdfFile(folder, projectView, projectVersionView)
        assertNotNull(pdfFile)
        assertTrue(pdfFile.exists())
    }

    @Test
    @ExtendWith(TempDirectory.class)
    public void createReportFilesTest(@TempDirectory.TempDir Path folderForReport) {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT")
        final String testProjectVersionName = restConnectionTestHelper.getProperty("TEST_VERSION")

        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory()
        ProjectService projectService = blackDuckServicesFactory.createProjectService()
        Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion(testProjectName, testProjectVersionName)
        ReportService riskReportService = blackDuckServicesFactory.createReportService(30000)
        ProjectView projectView = projectVersionWrapper.get().getProjectView()
        ProjectVersionView projectVersionView = projectVersionWrapper.get().getProjectVersionView()

        File reportFolder = folderForReport.toFile()
        riskReportService.createReportFiles(reportFolder, projectView, projectVersionView)

        File[] reportFiles = reportFolder.listFiles();
        assertNotNull(reportFiles)
        assertTrue(reportFiles.size() > 0)
        Map<String, File> reportFileMap = reportFiles.collectEntries {
            [it.getName(), it]
        }
        assertNotNull(reportFileMap.get('js'))
        assertNotNull(reportFileMap.get('css'))
        assertNotNull(reportFileMap.get('images'))
        assertNotNull(reportFileMap.get('riskreport.html'))
    }

    @Test
    @ExtendWith(TempDirectory.class)
    public void createNoticesReportFileTest(@TempDirectory.TempDir Path folderForReport) {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT")
        final String testProjectVersionName = restConnectionTestHelper.getProperty("TEST_VERSION")

        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory()
        ProjectService projectService = blackDuckServicesFactory.createProjectService()
        Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion(testProjectName, testProjectVersionName)
        ReportService riskReportService = blackDuckServicesFactory.createReportService(30000)
        ProjectView projectView = projectVersionWrapper.get().getProjectView()
        ProjectVersionView projectVersionView = projectVersionWrapper.get().getProjectVersionView()

        File noticeReportFile = riskReportService.createNoticesReportFile(folderForReport.toFile(), projectView, projectVersionView);
        assertNotNull(noticeReportFile)
        assertTrue(noticeReportFile.exists())
    }

    @Test
    @Disabled
    public void createReportFilesManually() {
        // fill these values in with your particulars
        final String projectName = 'ek-test-risk-report'
        final String projectVersionName = '1'
        final String localPathForHtmlReport = '/Users/ekerwin/Documents/working/riskreport_html'
        final String localPathForPdfReport = '/Users/ekerwin/Documents/working/riskreport_pdf'
        final String localPathForNoticesReport = '/Users/ekerwin/Documents/working/notices'

        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory()
        final ProjectService projectService = blackDuckServicesFactory.createProjectService();
        Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion(projectName, projectVersionName)
        ProjectView projectView = projectVersionWrapper.get().getProjectView()
        ProjectVersionView projectVersionView = projectVersionWrapper.get().getProjectVersionView()
        ReportService riskReportService = blackDuckServicesFactory.createReportService(30000)

        File htmlReportFolder = new File(localPathForHtmlReport)
        File pdfReportFolder = new File(localPathForPdfReport)
        File noticesReportFolder = new File(localPathForNoticesReport)

        htmlReportFolder.mkdirs()
        pdfReportFolder.mkdirs()
        noticesReportFolder.mkdirs()

        riskReportService.createReportFiles(htmlReportFolder, projectView, projectVersionView)
        riskReportService.createReportPdfFile(pdfReportFolder, projectView, projectVersionView)
        riskReportService.createNoticesReportFile(noticesReportFolder, projectView, projectVersionView);
    }

}
