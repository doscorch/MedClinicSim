

import desmoj.core.simulator.*;
import desmoj.core.statistic.*;
import java.util.concurrent.TimeUnit;

/**
 * A model for running multiple experiments (simulation runs) in DESMO-J.
 * @author Jason Sauppe
 * Last Modified: 2018-11-28
 */
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
    protected ConfidenceCalculator repMaxInSystem;
    protected ConfidenceCalculator repAvgCustomerWaitingTime;
    protected ConfidenceCalculator repAvgNurseUtilization;
    protected ConfidenceCalculator repAvgSpecialistUtilization;

    /**
     * ReplicationModel constructor.
     *
     * @param owner the model this model is part of (set to null when there is
     *              no such model)
     * @param modelName this model's name
     * @param showInReport flag to indicate if this model shall produce output
     *                     to the report file
     * @param showInTrace flag to indicate if this model shall produce output
     *                    to the trace file
     */
    public ReplicationModel(Model owner, String modelName,
                     boolean showInReport, boolean showInTrace) {
        super(owner, modelName, showInReport, showInTrace);
    }

    /**
     * Returns a description of this model.
     * @return Model description
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
     * @param repNum The replication number to use
     */
    public void runSimulation(int repNum) {
        // Create an instance of the model and an experiment
        MedicalClinicModel model = new MedicalClinicModel(null,
                "Medical Clinic Model System", true, true);

        String outputFilename = "output/MedicalClinicModel" + "_Rep_" + repNum;
        Experiment exp = new Experiment(outputFilename,
                                        INCLUDE_REPORT_PER_REPLICATION);
        // Set the seed for the random number generator
        // (NOTE: Do this *before* connecting the experiment to the model)
        exp.setSeedGenerator(979 + 2*repNum);

        // Connect model and experiment
        model.connectToExperiment(exp);


        // Determine stopping conditions for the simulation:
        // - the CloseBankCondition will check if 8 hours have elapsed and
        //   all customers have left, at which point things should finish;
        // - As a fall-back, we also stop after 10 hours of simulated time,
        //   which should be more than enough to finish the remaining customers
        //   in the system after the doors close.
        exp.stop(new MedicalClinicModel.CloseBankCondition(model,
                    "Close Clinic", true));
        exp.stop(new TimeInstant(10, TimeUnit.HOURS));

        // set experiment parameters
        exp.setShowProgressBar(false);
        exp.traceOff(new TimeInstant(0));
        exp.debugOff(new TimeInstant(0));
        exp.setSilent(true);

        // start the experiment at simulation time 0.0
        exp.start();

        // --> now the simulation is running until it reaches its end criterion
        // ...
        // ...
        // <-- afterwards, the main thread returns here

        // generate the report (and other output files)
        if (INCLUDE_REPORT_PER_REPLICATION) {
            exp.report();
        }

        // Stop all threads still alive and close all output files
        exp.finish();

        // Update replication model statistics with replication result
        int maxInSystem = (int) model.customersInSystem.getMaximum();
        double avgWaitingTime = model.customerWaitTimes.getMean();
        double specialistUtilization = model.specialistUtilization.getMean();
        double nurseUtilization = model.nurseUtilization.getMean();
        repMaxInSystem.update(maxInSystem);
        repAvgCustomerWaitingTime.update(avgWaitingTime);
        repAvgNurseUtilization.update(nurseUtilization);
        repAvgSpecialistUtilization.update(specialistUtilization);
        // Print replication results; we'll also include the time at which the
        // simulation stopped as well as the number of customers left in the
        // system (which should always be 0) to double-check that the stopping
        // conditions are working correctly.
        if (INCLUDE_OUTPUT_PER_REPLICATION) {
            System.out.format("Rep. %4d: %2d, %7.3f, %.3f, %.3f, %5.1f, %d\n", repNum,
                maxInSystem, avgWaitingTime, nurseUtilization, specialistUtilization,
                exp.getSimClock().getTime().getTimeAsDouble(),
                (int) model.customersInSystem.getValue());
        }
    }

    /**
     * Initializes static model components like statistics.
     */
    public void init() {
        // Initialize trackers
        repMaxInSystem = new ConfidenceCalculator(this,
                "Per Replication: Max in System", true, false);
        repAvgCustomerWaitingTime = new ConfidenceCalculator(this,
                "Per Replication: Avg. Customer Waiting Time", true, false);
        repAvgNurseUtilization = new ConfidenceCalculator(this,
                "Per Replication: Avg. Nurse Utilization", true, false);
        repAvgSpecialistUtilization = new ConfidenceCalculator(this,
                "Per Replication: Avg. Specialist Utilization", true, false);
    }

    /**
     * Runs the model.
     *
     * @param args is an array of command-line arguments (ignored here)
     */
    public static void main(String[] args) {
        // Added to get proper display
        Experiment.setReferenceUnit(TimeUnit.MINUTES);

        // create the model and experiment and connect them
        ReplicationModel repModel = new ReplicationModel(null,
                "Replication Model for a Medical Clinic Model",
                true, true);
        Experiment exp = new Experiment("MedicalClinicReplication");
        repModel.connectToExperiment(exp);

        // set experiment parameters
        exp.setShowProgressBar(false);  // display a progress bar (or not)
        exp.stop(new TimeInstant(0, TimeUnit.MINUTES));

        // set the period of the trace and debug
        exp.traceOff(new TimeInstant(0));
        exp.debugOff(new TimeInstant(0));

        // start the experiment at simulation time 0.0
        exp.setSilent(true);
        exp.start();

        // --> now the simulation is running until it reaches its end criterion
        // ...
        // ...
        // <-- afterwards, the main thread returns here

        // generate the report (and other output files)
        exp.report();

        // stop all threads still alive and close all output files
        exp.finish();
    }

}

