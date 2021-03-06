package net.thucydides.reports.dashboard.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver

public class DashboardPage {

    private WebDriver driver;
    private final String dashboardPath

    DashboardPage(File dashboard) {
        dashboardPath = dashboard.absolutePath
    }

    def open() {
        driver = new ChromeDriver()
        driver.get("file://" + dashboardPath)
    }

    def close() {
        driver.quit()
    }


    def getDashboardTitle() {
        driver.findElement(By.cssSelector(".dashboard-header")).text
    }


    def getProjectHeadings() {
        driver.findElements(By.cssSelector(".nav .nav-header")).collect { it.text }
    }

    def getProjectSubheadings() {
        driver.findElements(By.cssSelector(".nav .nav-subsection")).collect { it.text }
    }

    def getGraphHeadings() {
        driver.findElements(By.cssSelector("h2")).collect { it.text }
    }

    def getGraphDataPoints() {
        driver.findElements(By.cssSelector(".jqplot-pie-series")).collect { it.text }
    }

    def selectProject(index) {
        driver.findElements(By.cssSelector(".nav .nav-header a")).get(index).click()
    }

    def getTitle() { driver.title }

}
