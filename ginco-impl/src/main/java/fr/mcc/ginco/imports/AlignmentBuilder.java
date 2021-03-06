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

package fr.mcc.ginco.imports;

import com.hp.hpl.jena.rdf.model.Statement;
import fr.mcc.ginco.ark.IIDGeneratorService;
import fr.mcc.ginco.beans.*;
import fr.mcc.ginco.services.IExternalThesaurusService;
import fr.mcc.ginco.services.IExternalThesaurusTypeService;
import fr.mcc.ginco.services.IThesaurusConceptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder in charge of building concept alignments
 */
@Service("skosAlignmentBuilder")
public class AlignmentBuilder extends AbstractBuilder {

	private static Logger logger = LoggerFactory.getLogger(AlignmentBuilder.class);

	@Inject
	@Named("generatorService")
	private IIDGeneratorService generatorService;

	@Inject
	@Named("thesaurusConceptService")
	private IThesaurusConceptService thesaurusConceptService;

	@Inject
	@Named("externalThesaurusTypeService")
	private IExternalThesaurusTypeService externalThesaurusTypeService;

	@Inject
	@Named("externalThesaurusService")
	private IExternalThesaurusService externalThesaurusService;

	private Pattern arkPt = Pattern
			.compile("(http|https)\\:\\/\\/[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}[\\/(\\S*)\\/]*\\/ark\\:\\/(\\S*)\\/");

	private Pattern urlPt = Pattern
			.compile("(http|https)\\:\\/\\/[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}[\\/(\\S*)\\/]*\\/(\\S*)\\/");


	public Alignment buildAlignment(Statement stmt,
	                                AlignmentType alignmentType, ThesaurusConcept concept) {
		logger.debug("Building alignment " + stmt.getObject().toString());

		Alignment alignment = new Alignment();
		alignment.setIdentifier(generatorService.generate(Alignment.class));
		alignment.setAlignmentType(alignmentType);
		alignment.setAndRelation(false);
		alignment.setCreated(concept.getCreated());
		alignment.setModified(concept.getModified());
		alignment.setSourceConcept(concept);

		if(alignmentType.isResource()){

			Set<AlignmentResource> targetResources = new HashSet<AlignmentResource>();
			AlignmentResource targetResource = new AlignmentResource();

			String externalTargetResource = stmt.getObject().toString();

			targetResource.setAlignment(alignment);
			targetResource.setExternalTargetResource(externalTargetResource);

			targetResources.add(targetResource);
			alignment.setTargetResources(targetResources);

		}else{

			Set<AlignmentConcept> targetConcepts = new HashSet<AlignmentConcept>();

			AlignmentConcept targetConcept = new AlignmentConcept();
			targetConcept.setAlignment(alignment);

			String targetConceptId = stmt.getObject().toString();
			ThesaurusConcept internalTargetConcept = thesaurusConceptService.getThesaurusConceptById(targetConceptId);
			if (internalTargetConcept != null) {
				targetConcept.setInternalTargetConcept(internalTargetConcept);
				alignment.setInternalTargetThesaurus(internalTargetConcept.getThesaurus());
			} else {
				// Check if the concept has an ark indentifier.

				Matcher arkMt = arkPt.matcher(targetConceptId);
				Matcher urlMt = urlPt.matcher(targetConceptId);

				if (arkMt.find()) {
					setExternalThesaurus(alignment, arkMt.group());
				} else if (urlMt.find()) {
					setExternalThesaurus(alignment, urlMt.group());
				}
				targetConcept.setExternalTargetConcept(targetConceptId);
			}

			targetConcepts.add(targetConcept);
			alignment.setTargetConcepts(targetConcepts);
		}
		return alignment;
	}

	private void setExternalThesaurus(Alignment alignment, String extThesaurusId) {
		ExternalThesaurus existingExternalThes = externalThesaurusService
				.getThesaurusByExternalId(extThesaurusId);
		if (existingExternalThes == null) {
			ExternalThesaurus externalThesaurus = new ExternalThesaurus();
			externalThesaurus.setExternalId(extThesaurusId);
			externalThesaurus
					.setExternalThesaurusType(externalThesaurusTypeService
							.getExternalThesaurusTypeList().get(0));
			alignment.setExternalTargetThesaurus(externalThesaurus);
		} else {
			alignment.setExternalTargetThesaurus(existingExternalThes);
		}
	}
}
