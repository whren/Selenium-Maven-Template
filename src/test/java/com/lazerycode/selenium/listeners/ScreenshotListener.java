package com.lazerycode.selenium.listeners;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Boolean;

import static com.lazerycode.selenium.DriverFactory.getDriver;

public class ScreenshotListener extends TestListenerAdapter {
    private boolean createFile(File screenshot) {
        boolean fileCreated = false;

        if (screenshot.exists()) {
            fileCreated = true;
        } else {
            File parentDirectory = new File(screenshot.getParent());
            if (parentDirectory.exists() || parentDirectory.mkdirs()) {
                try {
                    fileCreated = screenshot.createNewFile();
                } catch (IOException errorCreatingScreenshot) {
                    errorCreatingScreenshot.printStackTrace();
                }
            }
        }

        return fileCreated;
    }

    private void writeScreenshotToFile(WebDriver driver, File screenshot) {
        try {
            FileOutputStream screenshotStream = new FileOutputStream(screenshot);
            screenshotStream.write(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
            screenshotStream.close();
        } catch (IOException unableToWriteScreenshot) {
            System.err.println("Unable to write " + screenshot.getAbsolutePath());
            unableToWriteScreenshot.printStackTrace();
        }
    }

    @Override
    public void onTestFailure(ITestResult failingTest) {
        boolean screenshotEnabled = (null == System.getProperty("screenshot") ? false : Boolean.valueOf(System.getProperty("screenshot")));
        if (screenshotEnabled) {
            try {
                WebDriver driver = getDriver();
                String screenshotDirectory = System.getProperty("screenshotDirectory");
                if (null == screenshotDirectory) {
                    screenshotDirectory = new File("").getAbsolutePath() + File.separator + "target" + File.separator + "failsafe-reports";
                }
                String screenshotFileName = System.currentTimeMillis() + "_" + failingTest.getName() + ".png";
                String screenshotAbsolutePath = screenshotDirectory + File.separator + screenshotFileName;
                File screenshot = new File(screenshotAbsolutePath);
                if (createFile(screenshot)) {
                    try {
                        writeScreenshotToFile(driver, screenshot);
                    } catch (ClassCastException weNeedToAugmentOurDriverObject) {
                        writeScreenshotToFile(new Augmenter().augment(driver), screenshot);
                    }
                    System.out.println("Written screenshot to " + screenshotAbsolutePath);
                    Reporter.log("<a href=\"" + screenshotFileName + "\"><p align=\"left\">Add New PR screenshot at " + new Date()+ "</p>");
                } else {
                    System.err.println("Unable to create " + screenshotAbsolutePath);
                }
            } catch (Exception ex) {
                System.err.println("Unable to capture screenshot...");
                ex.printStackTrace();
            }
        }
    }
}