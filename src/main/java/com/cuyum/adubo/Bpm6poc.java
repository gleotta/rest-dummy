package com.cuyum.adubo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

public class Bpm6poc {

    private static Logger log = Logger.getLogger(Bpm6poc.class);

	public static void main(String[] args){
		URL url = null;
		try {
			// creamos la conexion al servidor bmp6 de cuyum
			url = new URL("http://162.243.12.101:8080/business-central/");
		} catch (MalformedURLException e) {
			log.error("La URL suministrada es incorrecta!",e);
		}
		
		/*--------------------------------------------*/
		//iniciamos la prueba de concepto del proceso "registro-operadores"
		Bpm6poc poc = new Bpm6poc(url);
		poc.connect();
		
		//creamos la instancia
		ProcessInstance instance = poc.crear_instancia();
		System.out.println("Estado de la instancia del proceso: "+instance.getState());
		List<TaskSummary> tareas = new ArrayList<TaskSummary>(); 
		tareas = poc.obtener_tareas(instance.getId());
		
		poc.verificarSolicitudPorAnalista(tareas.get(0));
		tareas = poc.obtener_tareas(instance.getId());
		
		poc.destrabarEsperarAlSolicitante(tareas.get(0));
		tareas = poc.obtener_tareas(instance.getId());
		
		poc.verificarSolicitudPorPlataforma(tareas.get(0));
		tareas = poc.obtener_tareas(instance.getId());
		
		poc.completarRegistro(tareas.get(0));
		/*--------------------------------------------*/
		
		poc.obtener_instancias_activas();
	}
	
	/* POC */

    private String deploymentId = "org.kie.proceso:aduanabo:1.0";
    private String definitionId = "bo.gob.aduana.registro-operadores";
    private URL deploymentUrl;

    private String userId = "viviana"; //usuario con roles: "tecAnalista", "tecPlataforma" y "Solicitante"  ;
    private String password = "cuyum123.";
    
    private RuntimeEngine engine;
    private KieSession session;
    private TaskService taskService;

    public Bpm6poc(URL url) {
    	this.deploymentUrl = url;
    }
    
    public void connect(){
    	if(deploymentUrl!=null){
    		
    		// Creamos la sesion REST
    		//creamos factoria con timeout
    		RemoteConfiguration configuration = new RemoteConfiguration(deploymentId, deploymentUrl, userId, password,30);
    		engine = new RemoteRuntimeEngine(configuration);
    		
    		//creamos factoria sin timeout
//    		RemoteRestRuntimeFactory restSessionFactory = new RemoteRestRuntimeFactory(deploymentId, deploymentUrl, userId, password);
//    		engine = restSessionFactory.newRuntimeEngine();
    		
    		session = engine.getKieSession();
    	}
    }

    public List<ProcessInstance> obtener_instancias_activas(){
    	List<ProcessInstance> instances = new ArrayList<ProcessInstance>();
    	instances.addAll(session.getProcessInstances());
    	
    	if(instances.isEmpty()){
    		System.out.println("No ha instancias activas de procesos.");
    		return instances;
    	}
    	for (ProcessInstance processInstance : instances) {
    		System.out.println("================================");
    		System.out.println("Proceso: "+processInstance.getProcessId());
    		System.out.println("Instancia: "+processInstance.getId());
    		System.out.println("Estado:"+processInstance.getState());
    		System.out.println("================================");
		}
    	return instances;
    }
    
    public ProcessInstance crear_instancia() {
		//Creamos una bolsa de parametros a enviar para iniciar la instancia
    	//estos parametros son necesarios para que el proceso corra
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("agenciaDesp", "false");
		params.put("idSolicitud", "tres-3");
		params.put("url", "http://localhost:8080/dummy/rest/service?idSolicitud=#{idSolicitud}");
		params.put("urlNotificacion", "http://localhost:8080/dummy/rest/service/date?idSolicitud=#{idSolicitud}");
		params.put("urlNotificacionProgramada", "http://localhost:8080/dummy/rest/service/date/random?idSolicitud=#{idSolicitud}");
		
		//iniciamos una instancia del proceso
		ProcessInstance processInstance = session.startProcess(definitionId, params);
		System.out.println("Started process instance: " + processInstance + " "
				+ (processInstance == null ? "" : processInstance.getId()));
		
		return processInstance;

    }
    
    public List<TaskSummary> obtener_tareas(long processInstance){
    	
    	//obtener una capa de servicios de tareas (TASKs)
		taskService = engine.getTaskService();
		
		//buscamos las tareas asignadas al grupo del usuario referenciado por "userId"
		List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(userId, "en-UK");
    	
    	//filtramos todas las tareas de la instancia recien creada
		List<TaskSummary> thisProcTasks = findTaskId(processInstance, tasks);
		
		return thisProcTasks;
    }
    
    public void verificarSolicitudPorAnalista(TaskSummary taskSummary){
		//se trata de obtener la tarea
		Task task = taskService.getTaskById(taskSummary.getId());
		System.out.println("Ubico la task " + task.getId() + ": " + taskSummary.getName() +" ("+taskSummary.getStatus()+")");
		
		//establecer que la tarea esta en progreso
		taskService.start(task.getId(), userId);
		
		//creo una bolsa de variables que permitiran el completado correcto de la tarea
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("observacionOut", "false");
		
		//reclamo la tarea
//		taskService.claim(task.getId(), userId);
		
		//completo la tarea
		taskService.complete(task.getId(), userId, taskParams);
		
    }
    
    public void destrabarEsperarAlSolicitante(TaskSummary taskSummary){
		//se trata de obtener la tarea
		Task task = taskService.getTaskById(taskSummary.getId());
		System.out.println("Ubico la task " + task.getId() + ": " + taskSummary.getName() +" ("+taskSummary.getStatus()+")");
		
		//establecer que la tarea esta en progreso
		taskService.start(task.getId(), userId);
		
		//creo una bolsa de variables que permitiran el completado correcto de la tarea
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("sePresentoOut", "si");
		
		//reclamo la tarea
//		taskService.claim(task.getId(), userId);
		
		//completo la tarea
		taskService.complete(task.getId(), userId, taskParams);
		
    }
    
    public void verificarSolicitudPorPlataforma(TaskSummary taskSummary){
		//se trata de obtener la tarea
		Task task = taskService.getTaskById(taskSummary.getId());
		System.out.println("Ubico la task " + task.getId() + ": " + taskSummary.getName() +" ("+taskSummary.getStatus()+")");
		
		//establecer que la tarea esta en progreso
		taskService.start(task.getId(), userId);
		
		//creo una bolsa de variables que permitiran el completado correcto de la tarea
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("observacionOut", "false");
		taskParams.put("editaFormOut", "false"); //el proceso terminará por "Completar Registro", no es necesario
		
		//reclamo la tarea
//		taskService.claim(task.getId(), userId);
		
		//completo la tarea
		taskService.complete(task.getId(), userId, taskParams);
		
    }
    
    public void completarRegistro(TaskSummary taskSummary){
		//se trata de obtener la tarea
		Task task = taskService.getTaskById(taskSummary.getId());
		System.out.println("Ubico la task " + task.getId() + ": " + taskSummary.getName() +" ("+taskSummary.getStatus()+")");
		
		//establecer que la tarea esta en progreso
		taskService.start(task.getId(), userId);
		
		//creo una bolsa de variables que permitiran el completado correcto de la tarea
		Map<String, Object> taskParams = new HashMap<String, Object>();
		
		//reclamo la tarea
//		taskService.claim(task.getId(), userId);
		
		//completo la tarea
		taskService.complete(task.getId(), userId, taskParams);
		
    }
    
    
    /* HELPER METHOD */
    private List<TaskSummary> findTaskId(long procInstId, List<TaskSummary> taskSumList) {
        List<TaskSummary> taskList = new ArrayList<TaskSummary>();
        for (TaskSummary task : taskSumList) {
        	System.out.println("Se encuentra el task "+task.getName()+" (" + task.getId() +") para el proceso "+procInstId);
            if (task.getProcessInstanceId() == procInstId) {
            	taskList.add(task); 
            }
        }
        return taskList;
    }

}