package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.InputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.CModelException;

/**
 * Defines behavior common to all C Model operations
 */
public abstract class CModelOperation implements IWorkspaceRunnable, IProgressMonitor {
	/**
	 * The elements this operation operates on,
	 * or <code>null</code> if this operation
	 * does not operate on specific elements.
	 */
	protected ICElement[] fElementsToProcess;

	/**
	 * The parent elements this operation operates with
	 * or <code>null</code> if this operation
	 * does not operate with specific parent elements.
	 */
	protected ICElement[] fParentElements;

	/**
	 * An empty collection of <code>ICElement</code>s - the common
	 * empty result if no elements are created, or if this
	 * operation is not actually executed.
	 */
	protected static ICElement[] fgEmptyResult= new ICElement[] {};

	/**
	 * Collection of <code>ICElementDelta</code>s created by this operation.
	 * This collection starts out <code>null</code> and becomes an
	 * array of <code>ICElementDelta</code>s if the operation creates any
	 * deltas. This collection is registered with the C Model notification
	 * manager if the operation completes successfully.
	 */
	protected ICElementDelta[] fDeltas= null;

	/**
	 * The elements created by this operation - empty
	 * until the operation actually creates elements.
	 */
	protected ICElement[] fResultElements= fgEmptyResult;

	/**
	 * The progress monitor passed into this operation
	 */
	protected IProgressMonitor fMonitor= null;

	/**
	 * A flag indicating whether this operation is nested.
	 */
	protected boolean fNested = false;

	/**
	 * Conflict resolution policy - by default do not force (fail on a conflict).
	 */
	protected boolean fForce= false;

	/*
	 * Whether the operation has modified resources, and thus whether resource
	 * delta notifcation will happen.
	 */
	protected boolean hasModifiedResource = false;

	protected CModelOperation() {
	}
	/**
	 * A common constructor for all C Model operations.
	 */
	protected CModelOperation(ICElement[] elements) {
		fElementsToProcess = elements;
	}

	/**
	 * Common constructor for all C Model operations.
	 */
	protected CModelOperation(ICElement[] elementsToProcess, ICElement[] parentElements) {
		fElementsToProcess = elementsToProcess;
		fParentElements= parentElements;
	}

	/**
	 * A common constructor for all C Model operations.
	 */
	protected CModelOperation(ICElement[] elementsToProcess, ICElement[] parentElements, boolean force) {
		fElementsToProcess = elementsToProcess;
		fParentElements= parentElements;
		fForce= force;
	}

	/**
	 * A common constructor for all C Model operations.
	 */
	protected CModelOperation(ICElement[] elements, boolean force) {
		fElementsToProcess = elements;
		fForce= force;
	}

	/**
	 * Common constructor for all C Model operations.
	 */
	protected CModelOperation(ICElement element) {
		fElementsToProcess = new ICElement[]{element};
	}

	/**
	 * A common constructor for all C Model operations.
	 */
	protected CModelOperation(ICElement element, boolean force) {
		fElementsToProcess = new ICElement[]{element};
		fForce= force;
	}

	/**
	 * Adds the given delta to the collection of deltas
	 * that this operation has created. These deltas are
	 * automatically registered with the C Model Manager
	 * when the operation completes.
	 */
	protected void addDelta(ICElementDelta delta) {
		if (fDeltas == null) {
			fDeltas= new ICElementDelta[] {delta};
		} else {
			ICElementDelta[] copy= new ICElementDelta[fDeltas.length + 1];
			System.arraycopy(fDeltas, 0, copy, 0, fDeltas.length);
			copy[fDeltas.length]= delta;
			fDeltas= copy;
		}
	}

	/**
	 * @see IProgressMonitor
	 */
	public void beginTask(String name, int totalWork) {
		if (fMonitor != null) {
			fMonitor.beginTask(name, totalWork);
		}
	}

	/**
	 * Checks with the progress monitor to see whether this operation
	 * should be canceled. An operation should regularly call this method
	 * during its operation so that the user can cancel it.
	 *
	 * @exception OperationCanceledException if cancelling the operation has been requested
	 * @see IProgressMonitor#isCanceled
	 */
	protected void checkCanceled() {
		if (isCanceled()) {
			throw new OperationCanceledException("operation.cancelled"); //$NON-NLS-1$
		}
	}

	/**
	 * Common code used to verify the elements this operation is processing.
	 * @see CModelOperation#verify()
	 */
	protected ICModelStatus commonVerify() {
		if (fElementsToProcess == null || fElementsToProcess.length == 0) {
			return new CModelStatus(ICModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
		}
		for (int i = 0; i < fElementsToProcess.length; i++) {
			if (fElementsToProcess[i] == null) {
				return new CModelStatus(ICModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
			}
		}
		return CModelStatus.VERIFIED_OK;
	}

	/**
	 * Convenience method to copy resources
	 */
	protected void copyResources(IResource[] resources, IPath destinationPath) throws CModelException {
		IProgressMonitor subProgressMonitor = getSubProgressMonitor(resources.length);
		IWorkspace workspace = resources[0].getWorkspace();
		try {
			workspace.copy(resources, destinationPath, false, subProgressMonitor);
			this.hasModifiedResource = true;
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	/**
	 * Convenience method to create a file
	 */
	protected void createFile(IContainer folder, String name, InputStream contents, boolean force) throws CModelException {
		IFile file= folder.getFile(new Path(name));
		try {
			file.create(contents, force, getSubProgressMonitor(1));
			this.hasModifiedResource = true;
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	/**
	 * Convenience method to create a folder
	 */
	protected void createFolder(IContainer parentFolder, String name, boolean force) throws CModelException {
		IFolder folder= parentFolder.getFolder(new Path(name));
		try {
			// we should use true to create the file locally. Only VCM should use tru/false
			folder.create(force, true, getSubProgressMonitor(1));
			this.hasModifiedResource = true;
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	/**
	 * Convenience method to delete a resource
	 */
	protected void deleteResource(IResource resource, boolean force) throws CModelException {
		try {
			resource.delete(force, getSubProgressMonitor(1));
			this.hasModifiedResource = true;
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	/**
	 * Convenience method to delete resources
	 */
	protected void deleteResources(IResource[] resources, boolean force) throws CModelException {
		if (resources == null || resources.length == 0) return;
		IProgressMonitor subProgressMonitor = getSubProgressMonitor(resources.length);
		IWorkspace workspace = resources[0].getWorkspace();
		try {
			workspace.delete(resources, force, subProgressMonitor);
			this.hasModifiedResource = true;
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	/**
	 * @see IProgressMonitor
	 */
	public void done() {
		if (fMonitor != null) {
			fMonitor.done();
		}
	}

	/**
	 * Verifies the operation can proceed and executes the operation.
	 * Subclasses should override <code>#verify</code> and
	 * <code>executeOperation</code> to implement the specific operation behavior.
	 *
	 * @exception CModelException The operation has failed.
	 */
	protected void execute() throws CModelException {
		ICModelStatus status= verify();
		if (status.isOK()) {
			executeOperation();
		} else {
			throw new CModelException(status);
		}
	}

	/**
	 * Convenience method to run an operation within this operation
	 */
	public void executeNestedOperation(CModelOperation operation, int subWorkAmount) throws CModelException {
		IProgressMonitor subProgressMonitor = getSubProgressMonitor(subWorkAmount);
		// fix for 1FW7IKC, part (1)
		try {
			operation.setNested(true);
			operation.run(subProgressMonitor);
			if (operation.hasModifiedResource()) {
				this.hasModifiedResource = true;
			}
			//accumulate the nested operation deltas
			if (operation.fDeltas != null) {
				for (int i = 0; i < operation.fDeltas.length; i++) {
					addDelta(operation.fDeltas[i]);
				}
			}
		} catch (CoreException ce) {
			if (ce instanceof CModelException) {
				throw (CModelException)ce;
			} else {
				// translate the core exception to a c model exception
				if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
					Throwable e = ce.getStatus().getException();
					if (e instanceof CModelException) {
						throw (CModelException) e;
					}
				}
				throw new CModelException(ce);
			}
		}
	}

	/**
	 * Performs the operation specific behavior. Subclasses must override.
	 */
	protected abstract void executeOperation() throws CModelException;

	/**
	 * Returns the elements to which this operation applies,
	 * or <code>null</code> if not applicable.
	 */
	protected ICElement[] getElementsToProcess() {
		return fElementsToProcess;
	}

	/**
	 * Returns the element to which this operation applies,
	 * or <code>null</code> if not applicable.
	 */
	protected ICElement getElementToProcess() {
		if (fElementsToProcess == null || fElementsToProcess.length == 0) {
			return null;
		}
		return fElementsToProcess[0];
	}

	/**
	 * Returns the C Model this operation is operating in.
	 */
	public ICModel getCModel() {
		if (fElementsToProcess == null || fElementsToProcess.length == 0) {
			return getParentElement().getCModel();
		} else {
			return fElementsToProcess[0].getCModel();
		}
	}

	/**
	 * Returns the parent element to which this operation applies,
	 * or <code>null</code> if not applicable.
	 */
	protected ICElement getParentElement() {
		if (fParentElements == null || fParentElements.length == 0) {
			return null;
		}
		return fParentElements[0];
	}

	/**
	 * Returns the parent elements to which this operation applies,
	 * or <code>null</code> if not applicable.
	 */
	protected ICElement[] getParentElements() {
		return fParentElements;
	}

	/**
	 * Returns the elements created by this operation.
	 */
	public ICElement[] getResultElements() {
		return fResultElements;
	}

	/**
	 * Creates and returns a subprogress monitor if appropriate.
	 */
	protected IProgressMonitor getSubProgressMonitor(int workAmount) {
		IProgressMonitor sub = null;
		if (fMonitor != null) {
			sub = new SubProgressMonitor(fMonitor, workAmount, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		}
		return sub;
	}

	/**
 	* Returns the <code>IWorkspace</code> this operation is working in, or
	 * <code>null</code> if this operation has no elements to process.
	 */
	protected IWorkspace getWorkspace() {
		if (fElementsToProcess != null && fElementsToProcess.length > 0) {
			ICProject project = fElementsToProcess[0].getCProject();
			if (project != null) {
				return project.getCModel().getWorkspace();
			}
		}
		return null;
	}

	/**
	 * Returns whether this operation has performed any resource modifications.
	 * Returns false if this operation has not been executed yet.
	 */
	public boolean hasModifiedResource() {
		return !this.isReadOnly() && this.hasModifiedResource;
	}

	public void internalWorked(double work) {
		if (fMonitor != null) {
			fMonitor.internalWorked(work);
		}
	}

	/**
	 * @see IProgressMonitor
	 */
	public boolean isCanceled() {
		if (fMonitor != null) {
			return fMonitor.isCanceled();
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if this operation performs no resource modifications,
	 * otherwise <code>false</code>. Subclasses must override.
	 */
	public boolean isReadOnly() {
		return false;
	}

	/**
	 * Convenience method to move resources
	 */
	protected void moveResources(IResource[] resources, IPath destinationPath) throws CModelException {
		IProgressMonitor subProgressMonitor = null;
		if (fMonitor != null) {
			subProgressMonitor = new SubProgressMonitor(fMonitor, resources.length, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		}
		IWorkspace workspace = resources[0].getWorkspace();
		try {
			workspace.move(resources, destinationPath, false, subProgressMonitor);
			this.hasModifiedResource = true;
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	/**
	 * Creates and returns a new <code>ICElementDelta</code>
	 * on the C Model.
	 */
	public CElementDelta newCElementDelta() {
		return new CElementDelta(getCModel());
	}

	/**
	 * Registers any deltas this operation created, with the
	 * C Model manager.
	 */
	protected void registerDeltas() {
		if (fDeltas != null && !fNested) {
			// hook to ensure working copies remain consistent
			//makeWorkingCopiesConsistent(fDeltas);
			CModelManager manager= CModelManager.getDefault();
			for (int i= 0; i < fDeltas.length; i++) {
				manager.registerCModelDelta(fDeltas[i]);
			}
		}
	}

	/**
	 * Main entry point for C Model operations.  Executes this operation
	 * and registers any deltas created.
	 *
	 * @see IWorkspaceRunnable
	 * @exception CoreException if the operation fails
	 */
	public void run(IProgressMonitor monitor) throws CoreException {
		try {
			fMonitor = monitor;
			execute();
		} finally {
			registerDeltas();
		}
	}

	/**
	 * @see IProgressMonitor
	 */
	public void setCanceled(boolean b) {
		if (fMonitor != null) {
			fMonitor.setCanceled(b);
		}
	}

	/**
	 * Sets whether this operation is nested or not.
	 * @see CreateElementInCUOperation#checkCanceled
	 */
	protected void setNested(boolean nested) {
		fNested = nested;
	}

	/**
	 * @see IProgressMonitor
	 */
	public void setTaskName(String name) {
		if (fMonitor != null) {
			fMonitor.setTaskName(name);
		}
	}

	/**
	 * @see IProgressMonitor
	 */
	public void subTask(String name) {
		if (fMonitor != null) {
			fMonitor.subTask(name);
		}
	}
	/**
	 * Returns a status indicating if there is any known reason
	 * this operation will fail.  Operations are verified before they
	 * are run.
	 *
	 * Subclasses must override if they have any conditions to verify
	 * before this operation executes.
	 *
	 * @see ICModelStatus
	 */
	protected ICModelStatus verify() {
		return commonVerify();
	}

	/**
	 * @see IProgressMonitor
	 */
	public void worked(int work) {
		if (fMonitor != null) {
			fMonitor.worked(work);
			checkCanceled();
		}
	}
}
