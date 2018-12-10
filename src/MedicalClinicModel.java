
/* Christian Strauss
 * Tristan Rooney
 * Dr Sauppe
 * CS351 - Project
 * MedicalClinicModel
 */

import desmoj.core.simulator.*;
import desmoj.core.dist.*;
import desmoj.core.statistic.*;
import java.util.concurrent.TimeUnit;

public class MedicalClinicModel extends Model {

	/* Constructor for model */
	public MedicalClinicModel(Model owner, String modelName, boolean showInReport, boolean showInTrace) {
		super(owner, modelName, showInReport, showInTrace);
	}

	/* State Variables */
	Nurse nurse;
	Nurse nurse2;
	Specialist specialist;
	Specialist specialist2;

	/* Queues */
	protected ProcessQueue<Customer> nurseQueue;
	protected ProcessQueue<Customer> specialistQueue;

	/* Random Distributions */
	public DiscreteDistUniform randomPercent;
	protected ContDistExponential nurseTreatmentDist;
	protected ContDistExponential specialistTreatmentDist;
	// 8 am to 10 am interarrival
	protected ContDistExponential interarrival8Dist;
	// 10 am to 4 pm interarrival
	protected ContDistExponential interarrival10Dist;
	// 4 pm to 8 pm interarrival
	protected ContDistExponential interarrival4Dist;

	/* Constants */
	protected final static int RUN_TIME = 720;

	/* Statistics */
	protected Count customersInSystem;
	protected Tally customerResponseTimes;
	protected Tally customerWaitTimes;
	protected Count customerSentToER;
	protected Accumulate nurseUtilization;
	protected Accumulate specialistUtilization;

	public String description() {
		return "Model Medical Clinic";
	}

	public void doInitialSchedules() {
		// initialize Nurse
		nurse = new Nurse(this, "Nurse", true);
		nurse.activate();
		// nurse2 = new Nurse(this, "Nurse2", true);
		// nurse2.activate();

		// initialize Specialist
		specialist = new Specialist(this, "Specialist", true);
		specialist.activate();
		// specialist2 = new Specialist(this, "Specialist2", true);
		// specialist2.activate();

		// initialize Customer Generator
		new CustomerGenerator(this, "Generator", true).activate();
	}

	public void init() {
		// Initialize state

		// Initialize queues
		nurseQueue = new ProcessQueue<Customer>(this, " Nurse queue ", true, false);
		specialistQueue = new ProcessQueue<Customer>(this, " Specialist queue ", true, false);

		// Initialize distributions
		interarrival8Dist = new ContDistExponential(this, "Interarrival", 15, true, false);
		interarrival10Dist = new ContDistExponential(this, "Interarrival", 6, true, false);
		interarrival4Dist = new ContDistExponential(this, "Interarrival", 9, true, false);
		nurseTreatmentDist = new ContDistExponential(this, "Nurse Treatment", 8, true, false);
		specialistTreatmentDist = new ContDistExponential(this, "Specialist Treatment", 25, true, false);

		randomPercent = new DiscreteDistUniform(this, "", 0, 100, true, false);

		// Initialize statistics
		customersInSystem = new Count(this, "Customers In System", true, true);
		customerResponseTimes = new Tally(this, "Customer Response Times", true, false);
		customerWaitTimes = new Tally(this, "Customer Wait Times", true, false);
		customerSentToER = new Count(this, "Customer Sent To ER", true, false);
		nurseUtilization = new Accumulate(this, "nurse Utilization", true, false);
		specialistUtilization = new Accumulate(this, "specialist Utilization", true, false);
	}

	public double sampleInterarrival() {
		if (presentTime().getTimeAsDouble() < 120) {
			return interarrival8Dist.sample();
		} else if (presentTime().getTimeAsDouble() < 480) {
			return interarrival10Dist.sample();
		} else {
			return interarrival4Dist.sample();
		}
	}

	public double sampleNurseTreatment() {
		return nurseTreatmentDist.sample();
	}

	public double sampleSpecialistTreatment() {
		return specialistTreatmentDist.sample();
	}

	public static void main(String[] args) {
		// INIT Experiment
		Experiment.setEpsilon(TimeUnit.MINUTES);
		Experiment.setReferenceUnit(TimeUnit.MINUTES);

		/* CREATE Model and Experiment */
		MedicalClinicModel model = new MedicalClinicModel(null, "Medical Clinic", true, true);
		Experiment exp = new Experiment(" Medical Clinic Simulation");
		model.connectToExperiment(exp);

		/* SET Experiment */
		exp.setShowProgressBar(false);
		exp.stop(new MedicalClinicModel.CloseBankCondition(model, "Close Clinic", true));
		exp.tracePeriod(new TimeInstant(0, TimeUnit.MINUTES), new TimeInstant(60, TimeUnit.MINUTES));
		exp.debugPeriod(new TimeInstant(0, TimeUnit.MINUTES), new TimeInstant(60, TimeUnit.MINUTES));

		/* RUN */
		exp.start();
		exp.report();
		exp.finish();
	}

	public static class CloseBankCondition extends ModelCondition {

		public CloseBankCondition(Model owner, String name, boolean showInTrace) {
			super(owner, name, showInTrace);
		}

		public boolean check() {
			MedicalClinicModel model = (MedicalClinicModel) getModel();
			return (model.presentTime().getTimeAsDouble(TimeUnit.MINUTES) > RUN_TIME
					&& model.customersInSystem.getValue() == 0);
		}
	}
}