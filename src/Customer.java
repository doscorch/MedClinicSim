
/* Christian Strauss
 * Tristan Rooney
 * Dr Sauppe
 * CS351 - Project
 * Customer
 */

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

public class Customer extends SimProcess {

	MedicalClinicModel model;
	private static final double BALK_MAX = 8.0;
	boolean needSpecialist;
	boolean leftSystem;
	double arrivalTime;
	double departureTime;
	double waitBegin;
	double waitTime;

	public Customer(Model model, String name, boolean showInTrace) {
		super(model, name, showInTrace);
		this.model = (MedicalClinicModel) model;
		needSpecialist = false;
		arrivalTime = 0;
		departureTime = 0;
		waitBegin = 0;
		waitTime = 0;
	}

	public void lifeCycle() throws SuspendExecution {
		// update statistics
		model.customersInSystem.update(1);
		arrivalTime = model.presentTime().getTimeAsDouble();

		if (!Balk()) {
			// Nurse
			waitBegin = model.presentTime().getTimeAsDouble();
			model.nurseQueue.insert(this);
			if (model.nurse.status == Status.idle) {
				model.nurse.activate();
			}
			// if (model.nurse2.status == Status.idle) {
			// model.nurse2.activate();
			// }
			passivate();

			// Specialist
			if (needSpecialist) {
				// fly em outa here
				if (model.presentTime().getTimeAsDouble() - arrivalTime >= 30) {
					model.customerSentToER.update(1);
					leftSystem = true;
				} else {
					if (model.specialistQueue.size() >= 3) {
						model.customerSentToER.update(1);
						leftSystem = true;
					} else {
						waitBegin = model.presentTime().getTimeAsDouble();
						model.specialistQueue.insert(this);
						if (model.specialist.status == Status.idle) {
							model.specialist.activate();
						}
						// if (model.specialist2.status == Status.idle) {
						// model.specialist2.activate();
						// }
						passivate();
					}
				}
			}

			departureTime = model.presentTime().getTimeAsDouble();
			if (!leftSystem) {
				model.customerResponseTimes.update(departureTime - arrivalTime);
				model.customerWaitTimes.update(waitTime);
			}
		} else {
			leftSystem = true;
		}
		// update statistics
		model.customersInSystem.update(-1);
	}

	public boolean Balk() {
		double sample = model.randomPercent.sample();
		System.out.println(sample);
		System.out.println(model.nurseQueue.size());
		return sample <= (((double) model.nurseQueue.size() / BALK_MAX) * 100);
	}
}
