package projectgeneratorplugin.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
@SuppressWarnings("restriction")
public class SampleHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		String projectName = "project.name";
		
		String workRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		String projectRoot = workRoot + "\\" + projectName;
		
		String mvn = "mvn archetype:generate -DgroupId=uo.tfm -DartifactId=" + projectName + " -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false";
		String commandCreate = "cmd /c cd " + workRoot + " & " + mvn;
		String commandEclipse = "cmd /c cd " + projectRoot + " & cmd /c dir & mvn eclipse:eclipse";
		
		executeCommand(commandCreate);
		executeCommand(commandEclipse);
		
		try {
			//testProject("test");
			importProject(projectName);
		} catch (CoreException e) {
			e.printStackTrace();
		}		
		
		MessageDialog.openInformation(
				window.getShell(),
				"ProjectGeneratorPlugin",
				"Creado projecto '" + projectName + "', agregando naturaleza maven y configurando");
		
		return null;
	}
	
	private void executeCommand(String command) {
		try {
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader input = new BufferedReader(new
					InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
			  System.out.println(line);
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void importProject(String projectName) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		project.create(null);
		project.open(null);
		addMavenNature(project);
	}
	
	private void addMavenNature(IProject project) throws CoreException{
	   IProjectDescription desc = project.getDescription();

	   String[] prevNatures = desc.getNatureIds();
	   String[] newNatures = new String[prevNatures.length + 1];

	   System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);

	   newNatures[prevNatures.length] = "org.eclipse.m2e.core.maven2Nature"; //add maven nature to the project
	   desc.setNatureIds(newNatures);

	   project.setDescription(desc, new NullProgressMonitor());

	   ICommand[] commands = desc.getBuildSpec();
	   List<ICommand> commandList = Arrays.asList( commands );
	   ICommand build = new BuildCommand();
	   build.setBuilderName("org.eclipse.m2e.core.maven2Builder"); //add maven builder to the project
	   List<ICommand> modList = new ArrayList<>( commandList );
	   modList.add( build );
	   desc.setBuildSpec( modList.toArray(new ICommand[]{}));
	}
	
}
