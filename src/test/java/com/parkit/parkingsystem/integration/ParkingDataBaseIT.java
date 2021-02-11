package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        //GIVEN
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    	//WHEN
    	parkingService.processIncomingVehicle();
        //THEN
    	Ticket resultTicket = ticketDAO.getTicket("ABCDEF"); //get the Ticket that has been saved in the database
    	
    	//check that a ticket is actually saved in DB:
    	assertEquals(1, resultTicket.getId()); //must be 1 since DB is empty
    	assertEquals(1, resultTicket.getParkingSpot().getId()); //must be 1 since it is the first slot available for TYPE=CAR in table parking
    	assertEquals(0, resultTicket.getPrice()); //must be 0 since outTime is unknown
    	//In this case we can not know the exact time the inTime value of Ticket is created, so for test purpose 
    	//i just check the time difference in Ticket is less than 5sec (5000 msec), seems enough margin for database simple write+read :
    	assertTrue(Math.abs((new Date()).getTime() - resultTicket.getInTime().getTime())<5000);
    	assertEquals(null,resultTicket.getOutTime()); //must be null since outTime is unknown
        
    	//check that Parking table is updated with availability:
    	assertEquals(2,parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)); //Since we are using slot1, the next available slot must be 2
    }

    @Test
    public void testParkingLotExit(){
        //GIVEN
    	testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //WHEN
        parkingService.processExitingVehicle();
        //THEN
        Ticket resultTicket = ticketDAO.getTicket("ABCDEF"); //get the Ticket that has been saved in the database
        //check that the fare generated correctly in db:
        assertTrue(0!=resultTicket.getPrice()); //must be different from 0 since ExitingVehicle has triggered fare calculation 
        //In this case we can not know the exact time the outTime value of Ticket is created, so for test purpose 
    	//i just check the time difference in Ticket is less than 5sec (5000 msec), seems enough margin for database simple write+read :
    	assertTrue(Math.abs((new Date()).getTime() - resultTicket.getOutTime().getTime())<5000);
        
    }

}
