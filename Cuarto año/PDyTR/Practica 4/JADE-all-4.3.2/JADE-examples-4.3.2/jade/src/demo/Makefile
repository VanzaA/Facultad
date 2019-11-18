# Makefile for JADE demo applications.

.PHONY:	all clean

all: MeetingSchedulerAgent
	@echo Making demo applications

MeetingSchedulerAgent:
	@echo Making Meeting Scheduler application
	$(JC) $(JFLAGS) -classpath $(CLASSPATH):$(SRCDIR):$(CLSDIR):$(ROOTDIR)/demo/CalendarBean.jar $(DEMODIR)/MeetingScheduler/MeetingSchedulerAgent.java

clean:
	rm -f `find . -name '*~' -print`
	rm -f `find . -name '#*#' -print`

batch:
	echo 'cd ..\demo' >> $(BATCH_EXAMPLES)
	echo '$(JC) $(subst /,\,$(subst $(ROOTDIR),..\..,$(JFLAGS))) -classpath %CLASSPATH%;..\..\src;..\..\classes;..\..\demo\CalendarBean.jar MeetingScheduler\MeetingSchedulerAgent.java' >> $(BATCH_EXAMPLES)
	echo >> $(BATCH_EXAMPLES)
	echo 'cd ..\..' >> $(BATCH_EXAMPLES)

