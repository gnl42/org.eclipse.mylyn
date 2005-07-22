/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jan 31, 2005
  */
package org.eclipse.mylar.core;

import java.util.List;

import org.eclipse.mylar.core.search.IMylarSearchOperation;

/**
 * @author Mik Kersten
 * 
 * TODO: should probably turn into an interface
 */
public abstract class AbstractRelationshipProvider implements IMylarContextListener {
	
    private boolean enabled = false;
    private String id;
    private String structureKind;
    private int degreeOfSeparation;
    
    public void nodeDeleted(IMylarContextNode node) {
    	// we don't care when this happens
    }

    public String getId() {
        return id;
    }
    
    public AbstractRelationshipProvider(String structureKind, String id) {
        this.id = id;
        this.structureKind = structureKind;
        degreeOfSeparation = MylarPlugin.getDefault().getPreferenceStore().getInt(getGenericId());
    }
   
    protected abstract void findRelated(final IMylarContextNode node, int degreeOfSeparation);
    
    /**
     * @param limitTo Only used in thye AbstractJavaRelationshipProvider for the search type
     */
    public abstract IMylarSearchOperation getSearchOperation(IMylarContextNode node, int limitTo, int degreeOfSeparation);

    public abstract String getName();
    
    public boolean acceptResultElement(Object element){
    	return true;
    }
    
    public void landmarkAdded(IMylarContextNode node) { 
        if (enabled) findRelated(node, degreeOfSeparation);
  } 
    
    public void landmarkRemoved(IMylarContextNode node) {
//        MylarPlugin.getTaskscapeManager().removeEdge(element, id);
    }
     
    /** 
     * TODO: plug this stuff into the persistent context properly
     * @param sourceNode TODO
     */
    protected void incrementInterest(IMylarContextNode node, String elementKind, String elementHandle, int degreeOfSeparation) {
        int predictedInterest = 1;//(7-degreeOfSeparation) * TaskscapeManager.getScalingFactors().getDegreeOfSeparationScale();
//    	((DegreeOfInterest)targetNode.getDegreeOfInterest()).addEvent(
        InteractionEvent event = new InteractionEvent(InteractionEvent.Kind.PREDICTION, elementKind, elementHandle, getSourceId(), getId(), null, predictedInterest);
        MylarPlugin.getContextManager().handleInteractionEvent(event);
        
//        CompositeContextNode targetNode = (CompositeContextNode)MylarPlugin.getContextManager().getNode(elementHandle);
//        MylarContextNode concreteTargetNode = null;
//        if (targetNode.getNodes().size() != 1) {
//        	return;
//        } else {
//        	concreteTargetNode = targetNode.getNodes().iterator().next();
//        }
//        if (concreteTargetNode != null) {
//	        for (MylarContextNode sourceNode : ((CompositeContextNode)node).getNodes()) {
//	        	
//	        	MylarContextEdge edge = new MylarContextEdge(elementKind, getId(), sourceNode, concreteTargetNode, sourceNode.getContext());
//	        	sourceNode.addEdge(edge);
//
//			}
//        }
//        InteractionEvent event = new InteractionEvent(InteractionEvent.Kind.PREDICTION, elementKind, elementHandle, getSourceId(), getId(), null, predictedInterest);
//        MylarPlugin.getContextManager().handleInteractionEvent(event);
    }
    
    protected abstract String getSourceId();

    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getCurrentDegreeOfSeparation(){
    	return degreeOfSeparation;
    }
    
    public void presentationSettingsChanging(UpdateKind kind) { 
    	// we don't care about this event
    }
    
    public void presentationSettingsChanged(UpdateKind kind) { 
    	// we don't care about this event
    }
    
    public void contextActivated(IMylarContext taskscape) { 
    	// we don't care about this event
    }

    public void contextDeactivated(IMylarContext taskscape) { 
    	// we don't care about this event
    }

    public void interestChanged(IMylarContextNode info) { 
    	// we don't care about this event
    }
    
    public void interestChanged(List<IMylarContextNode> nodes) { 
    	// we don't care about this event
    }

    public void relationshipsChanged() { 
    	// we don't care about this event
    }
    
    @Override
    public String toString() {
        return "(provider for: " + id.toString() + ")";
    }

    public String getStructureKind() {
        return structureKind;
    }

	public void setDegreeOfSeparation(int degreeOfSeparation){
		this.degreeOfSeparation = degreeOfSeparation;
		MylarPlugin.getDefault().getPreferenceStore().setValue(getGenericId(), degreeOfSeparation);
	}

	public abstract String getGenericId();
	
	public abstract void stopAllRunningJobs();
}
