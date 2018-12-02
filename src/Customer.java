/* Christian Strauss
 * Dr Sauppe
 * CS351 - Project
 * Medical Clinic
 * Customer
 */

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

public class Customer extends SimProcess {

	MedicalClinicModel model;
	private static final double BALK_MAX = 8.0;
	boolean needSpecialist;
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
		
		if( !Balk() ) {
			// Nurse
			waitBegin = model.presentTime().getTimeAsDouble();
			model.nurseQueue.insert(this);
			if (model.nurse.status == Status.idle) {
				model.nurse.activate();
			}
			passivate();
			
			// Specialist
			if(needSpecialist) {
				waitBegin = model.presentTime().getTimeAsDouble();
				model.specialistQueue.insert(this);
				if (model.specialist.status == Status.idle) {
					model.specialist.activate();
				}
				passivate();
			}
			
			departureTime = model.presentTime().getTimeAsDouble();
			model.customerResponseTimes.update(departureTime - arrivalTime);
			model.customerWaitTimes.update(waitTime);
		}

		// update statistics
		model.customersInSystem.update(-1);
	}
	
	public boolean Balk() {
		return Math.random() <= ((double) model.nurseQueue.size() / BALK_MAX);
	}
}
