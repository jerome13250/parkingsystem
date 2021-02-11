package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        //Calculate duration between OutTime and InTime in milliseconds :
        long durationInMilliesec = Math.abs(ticket.getOutTime().getTime() - ticket.getInTime().getTime());
        

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice( priceCalculate(durationInMilliesec, Fare.CAR_RATE_PER_HOUR));
                break;
            }
            case BIKE: {
            	ticket.setPrice( priceCalculate(durationInMilliesec, Fare.BIKE_RATE_PER_HOUR));
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
    
    private double priceCalculate(long durationInMilliesec, double fareRatePerHour) {
    	//Free 30-min parking:
    	if ( durationInMilliesec < (30*60*1000) ) {
    		return 0;
    	}
   	
    	return ((double)durationInMilliesec)/(3600*1000)*fareRatePerHour;
    }
    
}