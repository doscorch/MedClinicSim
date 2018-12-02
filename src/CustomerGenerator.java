/* Christian Strauss
 * Dr Sauppe
 * CS351 - Project
 * Medical Clinic
 * CustomerGeneration
 */

import desmoj.core.simulator.*;
import java.util.concurrent.TimeUnit;
import co.paralleluniverse.fibers.SuspendExecution;

public class CustomerGenerator extends SimProcess {

	public CustomerGenerator(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);
	}

	public void lifeCycle() throws SuspendExecution {
		MedicalClinicModel model = (MedicalClinicModel) getModel();
		model.nurseUtilization.update(0);
		model.specialistUtilization.update(0);
		while (true) {
			this.hold(new TimeSpan(model.sampleInterarrival(), TimeUnit.MINUTES));
			Customer customer = new Customer(model, "Customer", true);
			customer.activate();
		}
	}
}
