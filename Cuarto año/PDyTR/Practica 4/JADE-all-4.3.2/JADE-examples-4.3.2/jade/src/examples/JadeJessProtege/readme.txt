Oliver Hoffmann 10 May 2001
oliver@hoffmann.org

examples.JadeJessProtege package

SOFTWARE REQUIREMENTS:
This example requires JESS version 6.0 and up in order to be compiled. It has
been actually tested with version 6.0a6 and 6.0a5.
JESS can be downloaded at http://herzberg.ca.sandia.gov/jess/
If the rule file 'Jess.clp' is used, then at execution time, the example 
just requires JESS and JADE.
If the rule file 'JessProtege.clp' is used, instead, then at execution time, 
the example requires also JessTab (it has been tested with version 1.0beta1)
and Protege (it has been tested with version 1.6beta).
JessTab can be downloaded at the JESS web site under "user contributions"
Protege can be downloaded at http://protege.stanford.edu

DESCRIPTION:
This package includes examples for a closer integration of JADE with JESS, optionally also with protege. Aspects are

* starting a JADE agent from a running JESS engine
* interactively developing JADE agent behaviour from a JESS console
* protege ontology support for JADE agents in JESS rules

The rule file Jess.clp shows integration of JADE with JESS and the rule file JessProtege.clp shows the 3-way-integration of JADE, JESS and protege using the JessTab. JADE agents and JESS engines connected to JADE agents run in their own seperate threads of execution.

Usage:

(A) start jess.Main with the file Jess.clp as parameter
for instance: java jess.Main examples/JadeJessProtege/Jess.clp

or  

(B) use the batch command to load Jess.clp from jess.console or JessWin
for instance: (batch "examples/JadeJessProtege/Jess.clp")

or

(C) use the batch command to load Jess.clp or JessProtege.clp from the JessTab
for instance: (batch "examples/JadeJessProtege/JessProtege.clp")

It is assumed that JADE and JESS in their latest versions are installed and that the JadeJessProtege directory is on the classpath, for (C) it is assumed that protege and the JessTab are installed.

Test by sending a CFP message to the example agent, it should reply with a PROPOSE message, copying original content.

(A) and (B) show how JADE can be started from a running JESS engine
(C) shows in addition how protege ontologies can be used by JADE agents