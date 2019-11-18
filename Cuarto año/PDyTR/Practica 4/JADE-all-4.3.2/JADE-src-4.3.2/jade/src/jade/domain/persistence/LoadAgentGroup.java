/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/


package jade.domain.persistence;


import jade.content.AgentAction;


/**
  This class represents the <code>load-agent-group</code> action of
  the <code>JADE-Persistence</code> ontology.  This action can be
  requested to the JADE AMS to load a previously stored agent group on
  a given container set.

  @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public class LoadAgentGroup implements AgentAction {

    public LoadAgentGroup() {
    }

    public void setGroup(AgentGroup ag) {
	group = ag;
    }

    public AgentGroup getGroup() {
	return group;
    }

    public void setRepository(String r) {
	repository = r;
    }

    public String getRepository() {
	return repository;
    }


    private AgentGroup group;
    private String repository;

}
