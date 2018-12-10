
/* Christian Strauss
 * Tristan Rooney
 * Dr Sauppe
 * CS351 - Project
 * Nurse
 */

import java.util.concurrent.TimeUnit;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

public class Nurse extends SimProcess {
	MedicalClinicModel model;
	private static final double REFERRAL_PROB = 40.0;
	protected Status status;

	public Nurse(Model model, String name, boolean showInTrace) {
		super(model, name, showInTrace);
		this.status = Status.idle;
		this.model = (MedicalClinicModel) model;
	}

	public void lifeCycle() throws SuspendExecution {
		while (true) {
			if (model.nurseQueue.isEmpty()) {
				status = Status.idle;
				model.nurseUtilization.update(0);
				passivate();
			} else {
				status = Status.busy;
				Customer customer = model.nurseQueue.removeFirst();
				model.nurseUtilization.update(1);
				customer.waitTime += model.presentTime().getTimeAsDouble() - customer.waitBegin;
				hold(new TimeSpan(model.sampleNurseTreatment(), TimeUnit.MINUTES));
				if (refer()) {
					customer.needSpecialist = true;
				}
				customer.activate();
			}
		}
	}

	public boolean refer() {
		return model.randomPercent.sample() <= REFERRAL_PROB;
	}
}
