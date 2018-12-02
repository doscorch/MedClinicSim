/* Christian Strauss
 * Dr Sauppe
 * CS351 - Project
 * Medical Clinic
 * Specialist
 */

import java.util.concurrent.TimeUnit;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

public class Specialist extends SimProcess {
	MedicalClinicModel model;
	protected Status status;

	public Specialist(Model model, String name, boolean showInTrace) {
		super(model, name, showInTrace);
		this.status = Status.idle;
		this.model = (MedicalClinicModel) model;
	}

	public void lifeCycle() throws SuspendExecution {
		while (true) {
			if (model.specialistQueue.isEmpty()) {
				status = Status.idle;
				model.specialistUtilization.update(0);
				passivate();
			} else {
				status = Status.busy;
				Customer customer = model.specialistQueue.removeFirst();
				model.specialistUtilization.update(1);
				customer.waitTime += model.presentTime().getTimeAsDouble() - customer.waitBegin;
				hold(new TimeSpan(model.sampleSpecialistTreatment(), TimeUnit.MINUTES));
				customer.activate();
			}
		}
	}
}
