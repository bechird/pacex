package com.epac.cap.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.Job;
import com.epac.cap.model.Log;
import com.epac.cap.model.Machine;
import com.epac.cap.model.Roll;
import com.epac.cap.model.Station;
import com.epac.cap.model.StationCategory;
import com.epac.cap.repository.JobDAO;
import com.epac.cap.repository.LogDAO;
import com.epac.cap.repository.MachineDAO;
import com.epac.cap.repository.PartDAO;
import com.epac.cap.repository.RollDAO;
import com.epac.cap.repository.StationDAO;

/**
 * Interacts with Log data.  Uses LogDAO for entity persistence.
 * @author walid
 *
 */
@Service
public class ProductionReportsHandler {
	
	
	@Autowired
	private LogDAO logDAO;
	
	@Autowired
	private RollDAO rollDAO;
	
	@Autowired
	private StationDAO stationDAO;
	
	@Autowired
	private MachineDAO machineDAO;

	@Autowired
	private JobDAO jobDAO;
	
	@Autowired
	private PartDAO partDAO;
	
	@Autowired
	private JobHandler jobHandler;

	private static Logger logger = Logger.getLogger(ProductionReportsHandler.class);
  

	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public List<ProdReportLine> makeReport(Date startDateTime, Date endDateTime) {
	
			List<Log> logs = logDAO.readTaskEndedLogsBetween(startDateTime, endDateTime);			
			logger.debug("logs found:" + logs.size());

			
			
			//groups container
			Map<String, List<Log>> groups = new HashMap<String, List<Log>>();
			
			
			//caches
			List<Machine> machinesCache = new ArrayList<Machine>();
			List<Station> stationsCache = new ArrayList<Station>();
			List<Roll> rollsCache = new ArrayList<Roll>();
			List<Job> jobsCache = new ArrayList<Job>();
			
			for(Log l: logs) {
				//get machineId from the log
				String machineId = l.getMachineId();
				
				//get machine by id
				//look into the cache
				Machine machine = null;
				for(Machine m : machinesCache) {
					if(m.getMachineId().equals(machineId)) {
						machine = m;
						break;
					}
				}
				if(machine == null) {
					machine = machineDAO.read(machineId);
					machinesCache.add(machine);
				}
			
				
				//get station of the machine
				//look into the cache
				String stationId = machine.getStationId();
				Station station = null;
				for(Station s : stationsCache) {
					if(s.getStationId().equals(stationId)) {
						station = s;
						break;
					}
				}
				if(station == null) {
					station = stationDAO.read(stationId);
					stationsCache.add(station);
				}
				
				
				
				
				
				//see if we know the station of this machine
				if( machine.getMachineType()!= null && groups.containsKey(machine.getMachineType().getId()) ) {
					
					groups.get(machine.getMachineType().getId()).add(l);
				
				}else if( groups.containsKey(stationId) ) {					
					
					groups.get(stationId).add(l);
				
				}else {
					

					//if station category is 'PLOWFOLDER' and machine type is 'PLOWFOLDER' or 'FLYFOLDER' or "POPLINE"
					if(machine.getMachineType() != null && StationCategory.Categories.PLOWFOLDER.toString().equals(station.getStationCategoryId()) ) {
						groups.put(machine.getMachineType().getId(), new ArrayList<Log>());
						groups.get(machine.getMachineType().getId()).add(l);
						
					}else {
						//group by station
						groups.put(station.getStationId(), new ArrayList<Log>());
						groups.get(station.getStationId()).add(l);
					}

				}				
				
			}
			
			//remove duplicated rollId for 'PLOWFOLDER' or 'FLYFOLDER' or "POPLINE"
			for(String stationOrMachineId : groups.keySet()) {
				
				if( stationOrMachineId.equals("PLOWFOLDER") || stationOrMachineId.equals("FLYFOLDER") || stationOrMachineId.equals("POPLINE") ) {
					List<Log> logsList = groups.get(stationOrMachineId);	
					List<Log> processedLogs = new ArrayList<Log>();
					Set<Integer> rollsIdsSet = new HashSet<Integer>();
					
					for(Log currentLog : logsList) {
						Integer currentRollId = currentLog.getRollId();
						if(!rollsIdsSet.contains(currentRollId)) {
							rollsIdsSet.add(currentRollId);
							processedLogs.add(currentLog);
						}
					}
					
					groups.put(stationOrMachineId, processedLogs);
					
				}
				
			}

			//remove duplicated rollId for PRESS station
			for(String stationOrMachineId : groups.keySet()) {
				
				if( stationOrMachineId.equals("PRESS") ) {
					List<Log> logsList = groups.get(stationOrMachineId);	
					List<Log> processedLogs = new ArrayList<Log>();
					Set<Integer> rollsIdsSet = new HashSet<Integer>();
					
					for(Log currentLog : logsList) {
						Integer currentRollId = currentLog.getRollId();
						if(!rollsIdsSet.contains(currentRollId)) {
							rollsIdsSet.add(currentRollId);
							processedLogs.add(currentLog);
						}
					}
					
					groups.put(stationOrMachineId, processedLogs);
					
				}
				
			}

			//remove duplicated currentJobId for the rest of stations
			for(String stationOrMachineId : groups.keySet()) {
				
				if( !stationOrMachineId.equals("PRESS") && !stationOrMachineId.equals("PLOWFOLDER") && !stationOrMachineId.equals("FLYFOLDER") && !stationOrMachineId.equals("POPLINE")) {
					List<Log> logsList = groups.get(stationOrMachineId);	
					List<Log> processedLogs = new ArrayList<Log>();
					Set<Integer> jobsIdsSet = new HashSet<Integer>();
					
					for(Log currentLog : logsList) {
						Integer currentJobId = currentLog.getCurrentJobId();
						if(!jobsIdsSet.contains(currentJobId)) {
							jobsIdsSet.add(currentJobId);
							processedLogs.add(currentLog);
						}
					}
					
					groups.put(stationOrMachineId, processedLogs);
					
				}
				
			}
			
			//make report
			List<ProdReportLine> report = new ArrayList<ProdReportLine>();
			
			for(String stationOrMachineId : groups.keySet()) {
				
				ProdReportLine prodReportLine = new ProdReportLine();
				prodReportLine.setStationOrMachineName(stationOrMachineId);
				
				List<Log> logsOfStation = groups.get(stationOrMachineId);
				prodReportLine.setRollsNumber(logsOfStation.size());
				
				
				if( stationOrMachineId.equals("PRESS") || stationOrMachineId.equals("PLOWFOLDER") || stationOrMachineId.equals("FLYFOLDER") || stationOrMachineId.equals("POPLINE") ) {
					
					
					for(Log log : logsOfStation) {
						
						
						// update consumed time
						if(log.getRollId() != null) {
							
							//get roll bean
							Roll roll = null;
							for(Roll r : rollsCache) {
								if(r.getRollId() == log.getRollId()) {
									roll = r;
									break;
								}
							}
							if(roll == null) {
								roll = rollDAO.read(log.getRollId());
								rollsCache.add(roll);
							}
							
							
							
							
							//get all jobs
							SortedSet<Job> jobs = roll.getAlljobs();
							
							for(Job job : jobs) {
								if(stationOrMachineId.equals("PRESS")) {
									if(!job.getStationId().equals("PRESS")) continue;
								}else {
									if(!job.getStationId().equals("PLOWFOLDER")) continue;
								}
									
								
								
								
								try {
									Float[] jobHoursLengthAndImp = jobHandler.calculateJobHoursAndLength(partDAO.read(job.getPartNum()), (int) job.getQuantityProduced(), roll.getWidth());									
									prodReportLine.setHours(prodReportLine.getHours() +  jobHoursLengthAndImp[0]);
									prodReportLine.setUsedLength(prodReportLine.getUsedLength() +  jobHoursLengthAndImp[1]);
								} catch (PersistenceException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
						}		
						
					}
					
				}
				
				
				//---------------------------------------------------------------------------------------------------------------------------------------------------------------------
				//
				if( !stationOrMachineId.equals("PRESS") && !stationOrMachineId.equals("PLOWFOLDER") && !stationOrMachineId.equals("FLYFOLDER") && !stationOrMachineId.equals("POPLINE") ) {
					
					
					for(Log log : logsOfStation) {						
						
						// update consumed time
						if(log.getCurrentJobId() != null) {
							
							//get job
							Job job = null;
							for(Job j : jobsCache) {
								if(j.getJobId() == log.getCurrentJobId()) {
									job = j;
									break;
								}
							}
							if(job == null) {
								job = jobDAO.read(log.getCurrentJobId());
								jobsCache.add(job);
							}
	
							prodReportLine.setUsedLength(prodReportLine.getUsedLength() +  job.getQuantityProduced());
							
							
							float logHours = 0;
							try {
								logHours = (job.getQuantityProduced() * job.getHours()) / job.getQuantityNeeded();
								
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							if(Float.isNaN(logHours))logHours = 0;
							prodReportLine.setHours(prodReportLine.getHours() +  logHours);

							
						}		
						
					}
					
				}
				

				
				
				report.add(prodReportLine);
			}
			
			
			//round floats
			int pressIndex = -1;
			for(int i=0;i<report.size();i++) {
				
				ProdReportLine line = report.get(i);
				line.setHours( (float)((int)( line.getHours() *100f))/100f  );
				line.setUsedLength( (float)((int)( line.getUsedLength() *100f))/100f  );
				
				if(line.getStationOrMachineName().equals("PRESS"))pressIndex = i;
			}
			
			//move press to top
			if(pressIndex != -1) {
				ProdReportLine line = report.get(pressIndex);
				report.remove(pressIndex);
				report.add(0, line);
			}
			
		
			return report;
		
	}
	
	
	
	public class ProdReportLine{
		
		private String stationOrMachineName;
		private int rollsNumber = 0;
		private float usedLength = 0;
		private float hours = 0;
		
		
		public String getStationOrMachineName() {
			return stationOrMachineName;
		}
		public void setStationOrMachineName(String stationOrMachineName) {
			this.stationOrMachineName = stationOrMachineName;
		}
		public int getRollsNumber() {
			return rollsNumber;
		}
		public void setRollsNumber(int rollsNumber) {
			this.rollsNumber = rollsNumber;
		}
		public float getHours() {
			return hours;
		}
		public void setHours(float hours) {
			this.hours = hours;
		}
		public float getUsedLength() {
			return usedLength;
		}
		public void setUsedLength(float usedLength) {
			this.usedLength = usedLength;
		}		
		
		
	}
 
}


