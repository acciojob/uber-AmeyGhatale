package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		customerRepository2.deleteById(customerId);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> driverList=driverRepository2.findAll();

		Driver driver = null;
		for(Driver currDriver : driverList){
			if(currDriver.getCab().getAvailable()){
				if((driver == null) || (currDriver.getDriverId() < driver.getDriverId())){
					driver = currDriver;
				}
			}
		}
		if(driver==null) {
			throw new Exception("No cab available!");
		}

		Customer customer=customerRepository2.findById(customerId).get();
		int perKmRate=driver.getCab().getPerKmRate();
		driver.getCab().setAvailable(false);
		int bill=perKmRate*distanceInKm;
		TripBooking tripBooking=new TripBooking(fromLocation,toLocation,distanceInKm);
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(driver);
		tripBooking.setBill(bill);
		tripBooking.setStatus(TripStatus.CONFIRMED);



		//Driver driver=driverRepository2.findById(driverId).get();


		customer.getTripBookingList().add(tripBooking);
		driver.getTripBookingList().add(tripBooking);

		customerRepository2.save(customer);
		driverRepository2.save(driver);
		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.setCustomer(null);
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setBill(0);
		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBookingRepository2.save(tripBooking);
	}
}
