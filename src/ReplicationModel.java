
/* Christian Strauss
 * Tristan Rooney
 * Dr Sauppe
 * CS351 - Project
 * Medical Clinic Replication Model
 */
import desmoj.core.simulator.*;
import desmoj.core.statistic.*;
import java.util.concurrent.TimeUnit;

public class ReplicationModel extends Model {

	/**
	 * Program constants and config variables
	 */
	public static final int NUM_REPLICATIONS = 100;
	public static final boolean INCLUDE_OUTPUT_PER_REPLICATION = true;
	public static final boolean INCLUDE_REPORT_PER_REPLICATION = false;

	/**
	 * Trackers for the replication model.
	 */
	protected ConfidenceCalculator arrivals;
	protected ConfidenceCalculator balkers;
	protected ConfidenceCalculator sentToER;
	protected ConfidenceCalculator fullyTreated;
	protected ConfidenceCalculator responseTimes;
	protected ConfidenceCalculator repAvgNurseUtilization;
	protected ConfidenceCalculator repAvgSpecialistUtilization;
	protected ConfidenceCalculator waitingRoom;
	protected ConfidenceCalculator clinicExpenses;

	/**
	 * ReplicationModel constructor.
	 */
	public ReplicationModel(Model owner, String modelName, boolean showInReport, boolean showInTrace) {
		super(owner, modelName, showInReport, showInTrace);
	}

	/**
	 * Returns a description of this model.
	 */
	public String description() {
		return "A model for running multiple experiments in DESMO-J.";
	}

	/**
	 * Creates and runs the simulation the desired number of times.
	 */
	public void doInitialSchedules() {
		// Run a set of replications
		for (int r = 1; r <= NUM_REPLICATIONS; ++r) {
			runSimulation(r);
		}
	}

	/**
	 * Perform one run of the simulation model.
	 */
	public void runSimulation(int repNum) {
		// Create an instance of the model and an experiment
		MedicalClinicModel model = new MedicalClinicModel(null, "Medical Clinic Model System", true, true);

		String outputFilename = "output/MedicalClinicModel" + "_Rep_" + repNum;
		Experiment exp = new Experiment(outputFilename, INCLUDE_REPORT_PER_REPLICATION);

		// Set the seed for the random number generator
		exp.setSeedGenerator(979 + 2 * repNum);

		// Connect model and experiment
		model.connectToExperiment(exp);

		exp.stop(new MedicalClinicModel.CloseBankCondition(model, "Close Clinic", true));
		exp.stop(new TimeInstant(10, TimeUnit.HOURS));

		// set experiment parameters
		exp.setShowProgressBar(false);
		exp.traceOff(new TimeInstant(0));
		exp.debugOff(new TimeInstant(0));
		exp.setSilent(true);

		// start the experiment at simulation time 0.0
		exp.start();

		// generate the report (and other output files)
		if (INCLUDE_REPORT_PER_REPLICATION) {
			exp.report();
		}

		// Stop all threads still alive and close all output files
		exp.finish();

		double specialistUtilization = model.specialistUtilization.getMean();
		double nurseUtilization = model.nurseUtilization.getMean();
		long dailyCustArrivals = model.customersInSystem.getObservations();
		double dailyBalkers = model.customersInSystem.getObservations() - model.nurseQueue.getObservations();
		double avgSentToER = model.customerSentToER.getMaximum();
		long dailyFullyTreated = model.customerResponseTimes.getObservations();
		double dailyResponse = model.customerResponseTimes.getMean();
		double dailyWaitingRoom = model.nurseQueue.averageLength();

		// 1200 nurse salary
		double expenses = 1200 * 1;
		// double expenses = 1200 * 2; // for 2 nurses
		// 1500 specialist
		expenses += 1500 * 1;
		// expenses += 1500 * 2; // for 2 specialists
		// 100 per nurse patient
		expenses += 100 * model.nurseQueue.getObservations();
		// 200 per specialist patient
		expenses += 200 * model.specialistQueue.getObservations();
		// 300 per exam room
		expenses += 300 * 4;
		// 500 per ER and per balk
		expenses += 500 * avgSentToER;
		expenses += 500 * dailyBalkers;

		clinicExpenses.update(expenses);

		arrivals.update(dailyCustArrivals);
		balkers.update(dailyBalkers);
		sentToER.update(avgSentToER);
		fullyTreated.update(dailyFullyTreated);
		responseTimes.update(dailyResponse);
		repAvgNurseUtilization.update(nurseUtilization);
		repAvgSpecialistUtilization.update(specialistUtilization);
		waitingRoom.update(dailyWaitingRoom);

		if (INCLUDE_OUTPUT_PER_REPLICATION) {
			System.out.format("Rep. %4d: \n", repNum, exp.getSimClock().getTime().getTimeAsDouble(),
					(int) model.customersInSystem.getValue());
		}
	}

	/**
	 * Initializes static model components like statistics.
	 */
	public void init() {
		// Initialize trackers
		arrivals = new ConfidenceCalculator(this, "Patients", true, false);
		balkers = new ConfidenceCalculator(this, "Balkers", true, false);
		sentToER = new ConfidenceCalculator(this, "Sent to ER", true, false);
		fullyTreated = new ConfidenceCalculator(this, "Fully Treated", true, false);
		responseTimes = new ConfidenceCalculator(this, "Response Times", true, false);
		repAvgNurseUtilization = new ConfidenceCalculator(this, "Nurse Utilization", true, false);
		repAvgSpecialistUtilization = new ConfidenceCalculator(this, "Specialist Utilization", true, false);
		waitingRoom = new ConfidenceCalculator(this, "Waiting Room", true, false);
		clinicExpenses = new ConfidenceCalculator(this, "Clinic Expenses", true, false);
	}

	/**
	 * Runs the model.
	 */
	public static void main(String[] args) {
		// Added to get proper display
		Experiment.setReferenceUnit(TimeUnit.MINUTES);

		// create the model and experiment and connect them
		ReplicationModel repModel = new ReplicationModel(null, "Replication Model for a Medical Clinic Model", true,
				true);
		Experiment exp = new Experiment("MedicalClinicReplication");
		repModel.connectToExperiment(exp);

		// set experiment parameters
		exp.setShowProgressBar(false); // display a progress bar (or not)
		exp.stop(new TimeInstant(0, TimeUnit.MINUTES));

		// set the period of the trace and debug
		exp.traceOff(new TimeInstant(0));
		exp.debugOff(new TimeInstant(0));

		// start the experiment at simulation time 0.0
		exp.setSilent(true);
		exp.start();

		// generate the report (and other output files)
		exp.report();

		// stop all threads still alive and close all output files
		exp.finish();
	}

}
