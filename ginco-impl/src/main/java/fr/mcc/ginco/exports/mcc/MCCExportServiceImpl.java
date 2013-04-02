/**
 * Copyright or © or Copr. Ministère Français chargé de la Culture
 * et de la Communication (2013)
 * <p/>
 * contact.gincoculture_at_gouv.fr
 * <p/>
 * This software is a computer program whose purpose is to provide a thesaurus
 * management solution.
 * <p/>
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software. You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p/>
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited liability.
 * <p/>
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systemsand/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 * <p/>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.mcc.ginco.exports.mcc;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.springframework.stereotype.Service;

import fr.mcc.ginco.beans.Note;
import fr.mcc.ginco.beans.Thesaurus;
import fr.mcc.ginco.beans.ThesaurusArray;
import fr.mcc.ginco.beans.ThesaurusConcept;
import fr.mcc.ginco.beans.ThesaurusConceptGroup;
import fr.mcc.ginco.beans.ThesaurusTerm;
import fr.mcc.ginco.exceptions.BusinessException;
import fr.mcc.ginco.exports.IMCCExportService;
import fr.mcc.ginco.exports.result.bean.JaxbList;
import fr.mcc.ginco.exports.result.bean.MCCExportedThesaurus;
import fr.mcc.ginco.services.IThesaurusArrayService;
import fr.mcc.ginco.services.IThesaurusConceptGroupService;
import fr.mcc.ginco.services.IThesaurusConceptService;
import fr.mcc.ginco.services.IThesaurusTermService;
import fr.mcc.ginco.services.IThesaurusVersionHistoryService;

@Service("mccExportService")
public class MCCExportServiceImpl implements IMCCExportService {
	
	@Inject
	@Named("thesaurusConceptService")
	private IThesaurusConceptService thesaurusConceptService;

	@Inject
	@Named("thesaurusArrayService")
	private IThesaurusArrayService thesaurusArrayService;

	@Inject
	@Named("thesaurusConceptGroupService")
	private IThesaurusConceptGroupService thesaurusConceptGroupService;
	
	@Inject
	@Named("thesaurusTermService")
	private IThesaurusTermService thesaurusTermService;
	
	@Inject
	@Named("thesaurusVersionHistoryService")
	private IThesaurusVersionHistoryService thesaurusVersionHistoryService;
	
	@Inject
	@Named("mccConceptExporter")
	private MCCConceptExporter mccConceptExporter;

	@Inject
	@Named("mccTermExporter")
	private MCCTermExporter mccTermExporter;

	
	
	@Override
	public String getThesaurusExport(Thesaurus thesaurus)
			throws BusinessException, JAXBException {
		MCCExportedThesaurus thesaurusToExport = new MCCExportedThesaurus();
		String thesaurusId = thesaurus.getIdentifier();
		
		//---Exporting the thesaurus
		thesaurusToExport.setThesaurus(thesaurus);
		
		//Exporting the thesaurus' versions
		thesaurusToExport.setThesaurusVersions(thesaurusVersionHistoryService.getVersionsByThesaurusId(thesaurusId));
		
		//Exporting sandboxed terms
		List<ThesaurusTerm> sandboxedTerms = thesaurusTermService.getPaginatedThesaurusSandoxedTermsList(0, thesaurusTermService.getSandboxedTermsCount(thesaurusId).intValue(), thesaurusId);
		for (ThesaurusTerm thesaurusTerm : sandboxedTerms) {
			thesaurusToExport.getTerms().add(thesaurusTerm);
		}
		
		//---Exporting the terms and the concepts of the thesaurus
		List<ThesaurusConcept> concepts = thesaurusConceptService.getConceptsByThesaurusId(null, thesaurusId, true, false);
		for (ThesaurusConcept thesaurusConcept : concepts) {
			
			//Exporting terms of the concepts
			List<ThesaurusTerm> terms = thesaurusTermService.getTermsByConceptId(thesaurusConcept.getIdentifier());
			for (ThesaurusTerm thesaurusTerm : terms) {
				thesaurusToExport.getTerms().add(thesaurusTerm);
			}
			
			//Exporting term notes
			for (ThesaurusTerm thesaurusTerm : terms) {
				JaxbList<Note> termNotes = mccTermExporter.getExportTermNotes(thesaurusTerm);
				if (termNotes != null && !termNotes.isEmpty()) {
					thesaurusToExport.getTermNotes().put(thesaurusTerm.getIdentifier(), termNotes);					
				}
			}
			
			//Exporting concept notes
			JaxbList<Note> conceptNotes = mccConceptExporter.getExportConceptNotes(thesaurusConcept);
			if (conceptNotes != null && !conceptNotes.isEmpty()) {
				thesaurusToExport.getConceptNotes().put(thesaurusConcept.getIdentifier(), conceptNotes);
			}
			
			//Exporting the parent concepts
			thesaurusToExport.getHierarchicalRelationship().put(thesaurusConcept.getIdentifier(), mccConceptExporter.getExportHierarchicalConcepts(thesaurusConcept));
			
			//Exporting relative relationship
			thesaurusToExport.getAssociativeRelationship().put(thesaurusConcept.getIdentifier(), mccConceptExporter.getExportAssociativeRelationShip(thesaurusConcept));
		}
		
		//---Exporting the concepts arrays of the thesaurus
		List<ThesaurusArray> arrays = thesaurusArrayService.getAllThesaurusArrayByThesaurusId(thesaurusId);
		for (ThesaurusArray thesaurusArray : arrays) {
			thesaurusToExport.getConceptsArrays().add(thesaurusArray);
		}
		
		//---Exporting the concepts groups of the thesaurus
		List<ThesaurusConceptGroup> groups = thesaurusConceptGroupService.getAllThesaurusConceptGroupsByThesaurusId(thesaurusId);
		for (ThesaurusConceptGroup thesaurusGroup : groups) {
			thesaurusToExport.getConceptsGroups().add(thesaurusGroup);
		}
		
		//We encode it in XML
		return serializeToXmlWithJaxb(thesaurusToExport);
	}
	
	/**
	 * This method serialize in XML a Thesaurus with all its elements (terms, concepts, relationships, notes, etc.).
	 * @param thesaurusToExport
	 * @return
	 * @throws JAXBException
	 */
	private String serializeToXmlWithJaxb(MCCExportedThesaurus thesaurusToExport)
			throws JAXBException {
		//This method encodes a MCCExportedThesaurus in XML
		String result = null;
		JAXBContext context = JAXBContext
				.newInstance(MCCExportedThesaurus.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		marshaller.marshal(thesaurusToExport, output);
		result = output.toString();
		return result;
	}
}