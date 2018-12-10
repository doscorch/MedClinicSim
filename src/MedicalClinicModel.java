/* Christian Strauss
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
	Specialist specialist;

	/* Queues */
	protected ProcessQueue<Customer> nurseQueue;
	protected ProcessQueue<Customer> specialistQueue;

	/* Random Distributions */
	protected ContDistExponential nurseTreatmentDist;
	protected ContDistExponential specialistTreatmentDist;
	//	8 am to 10 am interarrival
	protected ContDistExponential interarrival8Dist;
	//	10 am to 4 pm interarrival
	protected ContDistExponential interarrival10Dist;
	//	4 pm to 8 pm interarrival
	protected ContDistExponential interarrival4Dist;

	/* Constants */
	protected final static int RUN_TIME = 720;

	/* Statistics */
	protected Count customersInSystem;
	protected Tally customerResponseTimes;
	protected Tally customerWaitTimes;
	protected Accumulate nurseUtilization;
	protected Accumulate specialistUtilization;

	public String description() {
		return "Model Medical Clinic";
	}

	public void doInitialSchedules() {
		// initialize Boris
		nurse = new Nurse(this, "Nurse", true);
		nurse.activate();

		// initialize Naina
		specialist = new Specialist(this, "Specialist", true);
		specialist.activate();

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

		// Initialize statistics
		customersInSystem = new Count(this, "Customers In System", true, true);
		customerResponseTimes = new Tally(this, "Customer Response Times", true, false);
		customerWaitTimes = new Tally(this, "Customer Wait Times", true, false);
		nurseUtilization = new Accumulate(this, "nurse Utilization", true, false);
		specialistUtilization = new Accumulate(this, "specialist Utilization", true, false);
	}

	public double sampleInterarrival() {
		if(presentTime().getTimeAsDouble() < 120) {
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
		exp.stop(new TimeInstant(RUN_TIME, TimeUnit.MINUTES));
		exp.tracePeriod(new TimeInstant(0, TimeUnit.MINUTES), new TimeInstant(60, TimeUnit.MINUTES));
		exp.debugPeriod(new TimeInstant(0, TimeUnit.MINUTES), new TimeInstant(60, TimeUnit.MINUTES));

		/* RUN */
		exp.start();
		exp.report();
		exp.finish();
	}
	
	public static class CloseBankCondition extends ModelCondition {

        public CloseBankCondition(Model owner, String name,
                                    boolean showInTrace) {
            super(owner, name, showInTrace);
        }

        /**
         * Returns true if the bank can be closed for the day, which occurs
         * after 8 hours have elapsed *and* all customers have finished.
         */
        public boolean check() {
        	MedicalClinicModel model = (MedicalClinicModel) getModel();
            return (model.presentTime().getTimeAsDouble(TimeUnit.HOURS) > 8
                    && model.customersInSystem.getValue() == 0);
        }
    }
}