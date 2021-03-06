package ss.gui;

import ss.api.Project;

public interface SimulationListener {

	public void updateIdleProgrammers(int qty);

	public void updateTime(int time);

	public void updateWorkingProgrammers(Project project);

	public void updateInvestment(Project project);

	public void updateIterationEstimate(Project project);

	public void updateFinishedProjects(int qty);

	public void updateIterationDuration(Project project);
	
	public void addProject(Project project);

	public void reset();
	
	public void finishProject(Project project);
	
}
