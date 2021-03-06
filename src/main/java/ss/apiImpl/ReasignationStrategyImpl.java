package ss.apiImpl;

import java.util.List;

import ss.api.Iteration;
import ss.api.Project;
import ss.api.ReasignationStrategy;

public class ReasignationStrategyImpl implements ReasignationStrategy {

	private boolean idleStrategy;
	private boolean switchStrategy;
	private boolean freelanceStrategy;

	public final static int IDLE_STRATEGY = 0;
	public final static int SWITCH_STRATEGY = 1;
	public final static int FREELANCE_STRATEGY = 2;

	public ReasignationStrategyImpl(boolean idleStrategy,
			boolean switchStrategy, boolean freelanceStrategy) {
		this.idleStrategy = idleStrategy;
		this.switchStrategy = switchStrategy;
		this.freelanceStrategy = freelanceStrategy;
	}

	@Override
	public int reasing(Project to, List<Project> from, int idleProgrammers) {
		int ret = 0;
		if (idleStrategy) {
			ret = idleStrategyReasign(to, idleProgrammers);
		}
		if (switchStrategy) {
			switchStrategyReasign(to, from);
		}
		if (freelanceStrategy) {
			freelanceStrategyReasign(to);
		}
		return ret;
	}

	@Override
	public String getStrategy() {
		if (idleStrategy && switchStrategy && freelanceStrategy) {
			return "freelance";
		}
		if (idleStrategy && switchStrategy) {
			return "switch";
		}

		return "available";
	}

	private int idleStrategyReasign(Project to, int idleProgrammers) {
		boolean delayed = true;
		int newProgrammers = 0;
		Iteration iteration = to.getCurrentIteration();
		int projectProgrammers = to.getProgrammersWorking();
		int newEstimateProgrammers = 0;
		int newBackEstimation = 0;
		int newFrontEstimation = 0;
		int newIterationEstimation = 0;
		while (newProgrammers < idleProgrammers
				&& delayed
				&& ((newProgrammers + projectProgrammers) < SimulatorImpl.MAX_PROGRAMMER_PER_PROJECT)) {
			newProgrammers++;
			newEstimateProgrammers = projectProgrammers + newProgrammers;
			newBackEstimation = DistributionManager.getInstance()
					.getLastingDaysForBackendIssue(newEstimateProgrammers);
			newFrontEstimation = DistributionManager.getInstance()
					.getLastingDaysForFrontendIssue(newEstimateProgrammers);
			newIterationEstimation = newBackEstimation + newFrontEstimation;

			delayed = iteration.isDelayedWith(newIterationEstimation);

		}
		if (newEstimateProgrammers == 0) {
			return 0;
		}
		to.addProgrammers(newProgrammers);
		iteration.setEstimate(newIterationEstimation);
		return newProgrammers;
	}

	private void switchStrategyReasign(Project to, List<Project> from) {
		int projectIndex = from.indexOf(to);
		boolean delayed = true;

		// Last proyect cant switch programmers with another project.
		if (projectIndex == from.size() - 1) {
			return;
		}
		Iteration iteration = to.getCurrentIteration();
		int projectProgrammers = to.getProgrammersWorking();
		int newProgrammers = 0;
		int newEstimateProgrammers = 0;
		int newBackEstimation = 0;
		int newFrontEstimation = 0;
		int newIterationEstimation = 0;

		// Perhaps the idleStrategy already fixed the estimate, so this is
		// calculated again
		delayed = iteration.isDelayedWith(iteration.getEstimate());

		// Iterates from minor priority to mayor
		while (delayed
				&& ((newProgrammers + projectProgrammers) < SimulatorImpl.MAX_PROGRAMMER_PER_PROJECT)) {
			for (int i = from.size() - 1; i >= projectIndex
					&& delayed
					&& ((newProgrammers + projectProgrammers) < SimulatorImpl.MAX_PROGRAMMER_PER_PROJECT); i--) {
				Project other = from.get(i);
				
				//Only swith non freelance programmers
				int programmersQty = other.getProgrammersWorking()
						- other.getFreelanceProgrammersWorking();

				if (programmersQty > 0) {
					other.removeProgrammer();
					newProgrammers++;
					newEstimateProgrammers = projectProgrammers
							+ newProgrammers;
					newBackEstimation = DistributionManager.getInstance()
							.getLastingDaysForBackendIssue(
									newEstimateProgrammers);
					newFrontEstimation = DistributionManager.getInstance()
							.getLastingDaysForFrontendIssue(
									newEstimateProgrammers);
					newIterationEstimation = newBackEstimation
							+ newFrontEstimation;

					delayed = iteration.isDelayedWith(newIterationEstimation);
				}
			}
			break;
		}

		if (newProgrammers > 0) {
			to.addProgrammers(newProgrammers);
			iteration.setEstimate(newIterationEstimation);
		}
	}

	private void freelanceStrategyReasign(Project to) {
		int maxInvestment = to.getMaxInvestment();
		boolean delayed = true;
		int newProgrammers = 0;
		int newEstimateProgrammers = 0;
		int newBackEstimation = 0;
		int newFrontEstimation = 0;
		int newIterationEstimation = 0;
		Iteration iteration = to.getCurrentIteration();
		int projectProgrammers = to.getProgrammersWorking();
		// Perhaps the idleStrategy and switchStrategy already fixed the
		// estimate, so this is
		// calculated again
		delayed = iteration.isDelayedWith(iteration.getEstimate());
		while (to.getFreelanceProgrammersWorking()+newProgrammers < maxInvestment
				&& delayed
				&& ((newProgrammers + projectProgrammers) < SimulatorImpl.MAX_PROGRAMMER_PER_PROJECT)) {
			newProgrammers++;
			newEstimateProgrammers = projectProgrammers + newProgrammers;
			newBackEstimation = DistributionManager.getInstance()
					.getLastingDaysForBackendIssue(newEstimateProgrammers);
			newFrontEstimation = DistributionManager.getInstance()
					.getLastingDaysForFrontendIssue(newEstimateProgrammers);
			newIterationEstimation = newBackEstimation + newFrontEstimation;

			delayed = iteration.isDelayedWith(newIterationEstimation);
		}

		if (newProgrammers > 0) {
			to.addFreelanceProgrammers(newProgrammers);
			iteration.setEstimate(newIterationEstimation);
		}
	}

	@Override
	public boolean isSwitchStrategy() {
		return switchStrategy;
	}

	@Override
	public boolean isFreelanceStrategy() {
		return freelanceStrategy;
	}

	@Override
	public boolean isIdleStrategy() {
		return idleStrategy;
	}

	public int getStrategyID() {
		if (getStrategy().equals("available")) {
			return IDLE_STRATEGY;
		}
		if (getStrategy().equals("switch")) {
			return SWITCH_STRATEGY;
		}
		return FREELANCE_STRATEGY;
	}

}
