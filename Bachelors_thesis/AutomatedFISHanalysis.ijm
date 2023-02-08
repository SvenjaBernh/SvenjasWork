// PopUp Choose directory
dir = getDirectory("Choose a Directory ");
setBatchMode(true);
count = 0;
n = 0;

// User Interface for Selecting Channels for Counting and DAPI measuring
Dialog.create("Select Channel")

Dialog.addChoice("Channel for DAPI:", newArray("C1", "C2", "C3", "C4","none"), "none");

Dialog.addCheckbox("Channel 1", false);
Dialog.addCheckbox("Channel 2", true);
Dialog.addCheckbox("Channel 3", false);
Dialog.addCheckbox("Channel 4", false);
Dialog.addCheckbox("Image has only one Channel", false);

Dialog.addChoice("Threshold Method", newArray("Bernsen","Contrast","Mean","Median","MidGrey","Niblack","Otsu","Phansalkar","Sauvola"), "Phansalkar");

Dialog.show();

Channel1 = Dialog.getCheckbox();
Channel2 = Dialog.getCheckbox();;
Channel3 = Dialog.getCheckbox();;;
Channel4 = Dialog.getCheckbox();;;;
OneChannel =Dialog.getCheckbox();;;;;

DapiChannel = Dialog.getChoice();

ThresholdMethod = Dialog.getChoice();;


processFiles(dir);
// show date and time, when Macro is finished 
	MonthNames = newArray("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec");
    DayNames = newArray("Sun", "Mon","Tue","Wed","Thu","Fri","Sat");
    getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
    TimeString ="Date: "+DayNames[dayOfWeek]+" ";
    if (dayOfMonth<10) {TimeString = TimeString+"0";}
    TimeString = TimeString+dayOfMonth+"-"+MonthNames[month]+"-"+year+"\nTime: ";
    if (hour<10) {TimeString = TimeString+"0";}
    TimeString = TimeString+hour+":";
    if (minute<10) {TimeString = TimeString+"0";}
    TimeString = TimeString+minute+":";
    if (second<10) {TimeString = TimeString+"0";}
    TimeString = TimeString+second;
	showMessage("Finished at:\n" + TimeString);

//processing all files in directory and subdirectory
function processFiles(dir) {
	list = getFileList(dir);
	for (i=0; i<list.length; i++) {
          if (endsWith(list[i], "/"))
              processFiles(""+dir+list[i]);
          else {
             showProgress(n++, count);
             path = dir+list[i];
             processFile(path);
             run("Close All");
          }
      }
  }

// process every single image
function processFile(path) {
	if (endsWith(path, ".tif")) {
		if(matches(path, "^.*ResultsC+[0-9]+.*$")){} else{
			open(path);
			title = getTitle();
			
			if(OneChannel == true) {
				ChooseChannel = "";
				AnalyzeImage();
			} else{
				run("Split Channels");
				if (Channel1 == true){
					ChooseChannel = "C1";
					AnalyzeImage();
				}
				
				if (Channel2 == true){
					ChooseChannel = "C2";
					AnalyzeImage();
				}
				
				if (Channel3 == true){
					ChooseChannel = "C3";
					AnalyzeImage();
				}
				
				if (Channel4 == true){
					ChooseChannel = "C4";
					AnalyzeImage();
				}
				
			}
		ChooseChannel = DapiChannel;
		DapiAnalysis();
		run("Close All");
      }
	}
  }


// Image Processing and Analyze Particles
function AnalyzeImage() {
	// Processing
	if (OneChannel != true) {
		selectWindow(ChooseChannel + "-" + title);
	}
	run("Smooth");
	run("Subtract Background...", "rolling=50 sliding");
	// Binary Image
	
	ThresholdString = "method=" + ThresholdMethod + " radius=15 parameter_1=0 parameter_2=0 white";
	run("Auto Local Threshold", ThresholdString);
	run("Invert LUT");
	setOption("BlackBackground", false);
	run("Erode");
	run("Dilate");
	// count Particles
	run("Clear Results");
	run("Analyze Particles...", "size=1.0-Infinity display clear include add");
	// save Data Particles
	run("Flatten");
	saveAs("Tiff", path + "Results" + ChooseChannel + ".tif");
	selectWindow("Results");
	saveAs("Results", path + "Results" + ChooseChannel + ".csv");
	close();
}

function DapiAnalysis() {
	if (DapiChannel != "none"){
		selectWindow(DapiChannel + "-" + title);
		
		// DAPI measuring
		run("Smooth");
		run("Enhance Contrast...", "saturated=5 equalize");
		run("Auto Threshold", "method=IsoData white");
		setOption("BlackBackground", false);
		run("Analyze Particles...", "size=25-Infinity display clear include summarize add");
		//save Data DAPI
		selectWindow("Summary");
		saveAs("Results", path + "ResultsDAPI"  + ".csv");
		close(title + "ResultsDAPI"  + ".csv");
		run("Flatten");
		saveAs("Tiff", path + "DAPIResults" + ChooseChannel + ".tif");
		close();
	}
}
