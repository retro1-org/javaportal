#
## JavaApplicationStub path = /System/Library/Frameworks/JavaVM.framework/Resources/MacOS/
#
PACKAGE = com/nn/osiris/ui

BASEJAVA =  CircularBuffer.java\
	SizeFilter.java\
	LabeledComponent.java\
	CommunicationsDialog.java\
	LevelOneParser2.java\
	PortalFrame.java\
	LevelOneKermit.java\
	LevelOneParser.java\
	LevelOneNetwork.java\
	JPortal.java\
	LevelOnePanel.java\
	WindowsFNT.java\
	BrowserLauncher.java\
	KermitDialog.java\
	Sixbit.java\
	ONetworkListener.java\
	KeyBarDialog.java\
	NovaKeysDialog.java\
	MacGlueInterface.java\
	MacGlueMRJ.java\
	MacGlue.java\
	Portal.java\
	JMFImplementer.java\
	JMFInterface.java\
	QuickTimeImplementer.java\
	QuickTimeInterface.java\
	OptionsDialog.java\
	FieldFilterInterface.java\
	FieldFilter.java\
	PrintInterface.java\
	PrintInterfaceMac.java\
	ConnectDialog.java\
	PortalConsts.java\
	Session.java\
	NetConnInterface.java\
	ScormInterface.java\
	Options.java

LIBS = libs/myxml.jar:libs/mrj.jar:libs/ui.jar:libs/QTJava.zip:libs/jmf.jar

javaportal.jar : class1 class2
	$(JAVA14)/bin/jar cmf manifest.txt javaportal.jar com Portal.class
#	$(JAVA14)/bin/jarsigner -storepass novanet javaportal.jar myalias
	cd jarfixed && $(JAVA14)/bin/jar uf ../javaportal.jar com javax org

# build this class with jdk1.3 because it uses old quicktime for java
# which is associated with the jdk1.3 install
$(PACKAGE)/QuickTimeImplementerOld.class : QuickTimeImplementerOld.java
	$(JAVA13)/bin/javac -d . -classpath libs/oldQTJava.zip:. QuickTimeImplementerOld.java

# build this class with jdk1.5 because it uses new features
$(PACKAGE)/NetConnModern.class : NetConnModern.java
	$(JAVA15)/bin/javac -d . -cp . NetConnModern.java

# build this class with jdk1.5 because it uses the scorm lib
# which was compiled with 1.5
$(PACKAGE)/ScormImplementer.class : ScormImplementer.java
	$(JAVA15)/bin/javac -d . -cp libs/scorm.jar:. ScormImplementer.java

class2: $(PACKAGE)/QuickTimeImplementerOld.class $(PACKAGE)/NetConnModern.class $(PACKAGE)/ScormImplementer.class

class1: $(BASEJAVA)
	$(JAVA14)/bin/javac -d . -g -classpath $(LIBS) $(BASEJAVA)

clean:
	rm -rf com javaportal.jar

run:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host localhost -port 6005

mesa:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host 10.32.1.34 -port 6005

drmesa:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host 10.160.15.131 -port 6005

devo:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host chnndevcentral1.inf.ncs.com -port 6005

ui:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host nn-ui.nn.com -port 23

intel:
#	$(JAVA16)/bin/java -cp javaportal.jar Portal -host localhost -port 7015
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host 165.193.215.158 -port 3999

home:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host localhost -port 6010

proxy:
	$(JAVA16)/bin/java -DsocksProxyHost=127.0.0.1 -cp javaportal.jar Portal -host nn-ui.nn.com -port 23

scorm:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host 10.32.1.33 -port 6005

applet:
	$(JAVA16)/bin/appletviewer -J-Djava.security.policy=policy woof.html

deploy:
	cp javaportal.jar /var/www/html/

tmac:
	scp javaportal.jar tmac:/Applications/WPORTAL/Portal.app/Contents/Resources/Java

imac:
	scp javaportal.jar imac:/Applications/WPORTAL/Portal.app/Contents/Resources/Java
	scp Info.plist imac:/Applications/WPORTAL/Portal.app/Contents/

prod1:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host novanet4.nn.com -port 80

iniu1:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host 159.182.36.39 -port 722

directprod1:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host 10.32.1.23 -port 6005

niu:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host 150.176.94.10 -port 722

drprod1:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host 165.193.215.146 -port 722
#	$(JAVA16)/bin/java -cp javaportal.jar Portal -host localhost -port 7015
#

# scorm1 production
tutor1:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host localhost -port 6035

# scorm2 production
tutor2:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host localhost -port 6045

# scorm server thru firewall
tutor3:
	$(JAVA16)/bin/java -cp javaportal.jar Portal -host 206.31.248.34 -port 80
