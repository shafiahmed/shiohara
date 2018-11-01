/*
 * Copyright (C) 2016-2018 the original author or authors. 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.viglet.shiohara.utils;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viglet.shiohara.persistence.model.folder.ShFolder;
import com.viglet.shiohara.persistence.model.object.ShObject;
import com.viglet.shiohara.persistence.model.post.ShPost;
import com.viglet.shiohara.persistence.model.post.ShPostAttr;
import com.viglet.shiohara.persistence.model.reference.ShReference;
import com.viglet.shiohara.persistence.model.site.ShSite;
import com.viglet.shiohara.persistence.repository.object.ShObjectRepository;
import com.viglet.shiohara.persistence.repository.post.ShPostAttrRepository;
import com.viglet.shiohara.persistence.repository.post.ShPostRepository;
import com.viglet.shiohara.persistence.repository.reference.ShReferenceRepository;
import com.viglet.shiohara.post.type.ShSystemPostType;
import com.viglet.shiohara.widget.ShSystemWidget;

/**
 * Post Utils.
 *
 * @author Alexandre Oliveira
 * @since 0.3.0
 */
@Component
public class ShPostUtils {
	private static final Log logger = LogFactory.getLog(ShPostUtils.class);
	
	@Autowired
	private ShFolderUtils shFolderUtils;
	@Autowired
	private ShPostRepository shPostRepository;
	@Autowired
	private ShPostAttrRepository shPostAttrRepository;
	@Autowired
	private ShObjectRepository shObjectRepository;
	@Autowired
	private ShStaticFileUtils shStaticFileUtils;
	@Autowired
	private ShReferenceRepository shReferenceRepository;
	@Autowired
	private MongoTemplate mongoTemplate;

	public void saveDoc(ShPost shPost) {

		ObjectMapper mapper = new ObjectMapper();

		try {
			String jsonString = mapper.writeValueAsString(shPost);
			JSONObject jsonObject = new JSONObject(jsonString);
			jsonObject.put("_id", jsonObject.get("id"));
			jsonObject.remove("id");

			Document doc = Document.parse(jsonObject.toString());
			mongoTemplate.save(doc, "shPosts");
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * ShPostDoc shPostDoc = new ShPostDoc(); shPostDoc.setId(shPost.getId());
		 * Map<String, Object> attributes = new HashMap<String, Object>();
		 * Set<ShPostAttr> shPostAttrList = shPostAttrRepository.findByShPost(shPost);
		 * 
		 * for (ShPostAttr shPostAttr : shPostAttrList) { if
		 * (shPostAttr.getShPostTypeAttr().getShWidget().getName().equals(ShSystemWidget
		 * .RELATOR)) { Set<ShRelatorItem> shRelatorItems =
		 * shPostAttr.getShChildrenRelatorItems(); for (ShRelatorItem shRelatorItem :
		 * shRelatorItems) { Set<ShPostAttr> shPostAttrs =
		 * shRelatorItem.getShChildrenPostAttrs(); Map<String, Object> subAttributes =
		 * new HashMap<String, Object>(); for (ShPostAttr shPostAttrSub : shPostAttrs) {
		 * subAttributes.put(shPostAttrSub.getShPostTypeAttr().getName(),
		 * shPostAttrSub.getStrValue()); } attributes.put("relator", subAttributes); } }
		 * else { attributes.put(shPostAttr.getShPostTypeAttr().getName(),
		 * shPostAttr.getStrValue()); } }
		 * 
		 * shPostDoc.setAttributes(attributes); shPostDocRepository.save(shPostDoc);
		 */
	}

	public JSONObject toJSON(ShPost shPost) {
		JSONObject shPostItemAttrs = new JSONObject();

		JSONObject shPostObject = new JSONObject();
		shPostObject.put("id", shPost.getId());
		shPostObject.put("post-type-id", shPost.getShPostType().getId());
		shPostObject.put("title", shPost.getTitle());
		shPostObject.put("summary", shPost.getSummary());
		shPostObject.put("link", this.generatePostLink(shPost));
		for (ShPostAttr shPostAttr : shPost.getShPostAttrs()) {
			if (shPostAttr.getShPostTypeAttr().getName() != null) {
				shPostItemAttrs.put(shPostAttr.getShPostTypeAttr().getName(), shPostAttr.getStrValue());
			}
		}

		shPostItemAttrs.put("system", shPostObject);

		return shPostItemAttrs;
	}

	public Map<String, ShPostAttr> postToMap(ShPost shPost) {

		Set<ShPostAttr> shPostAttrList = shPostAttrRepository.findByShPost(shPost);

		Map<String, ShPostAttr> shPostMap = new HashMap<String, ShPostAttr>();
		ShPostAttr shPostAttrId = new ShPostAttr();
		shPostAttrId.setStrValue(shPost.getId().toString());
		shPostMap.put("id", shPostAttrId);
		for (ShPostAttr shPostAttr : shPostAttrList)
			shPostMap.put(shPostAttr.getShPostTypeAttr().getName(), shPostAttr);

		return shPostMap;

	}

	public ShPost copy(ShPost shPost, ShFolder shFolderDest) {

		ShPost shPostCopy = new ShPost();
		shPostCopy.setDate(new Date());
		shPostCopy.setShFolder(shFolderDest);
		shPostCopy.setShPostType(shPost.getShPostType());
		shPostCopy.setSummary(shPost.getSummary());
		shPostCopy.setTitle(shPost.getTitle());

		shPostRepository.save(shPostCopy);

		Set<ShPostAttr> shPostAttrs = shPostAttrRepository.findByShPost(shPost);
		for (ShPostAttr shPostAttr : shPostAttrs) {
			ShPostAttr shPostAttrClone = new ShPostAttr();
			shPostAttrClone.setDateValue(shPostAttr.getDateValue());
			shPostAttrClone.setIntValue(shPostAttr.getIntValue());
			shPostAttrClone.setReferenceObjects(new HashSet<ShObject>(shPostAttr.getReferenceObjects()));
			shPostAttrClone.setShPost(shPostCopy);
			shPostAttrClone.setShPostTypeAttr(shPostAttr.getShPostTypeAttr());
			shPostAttrClone.setStrValue(shPostAttr.getStrValue());
			shPostAttrClone.setType(shPostAttr.getType());
			shPostAttrRepository.save(shPostAttrClone);
		}

		return shPostCopy;
	}

	public String generatePostLink(ShPost shPost) {
		ShFolder shFolder = shPost.getShFolder();
		String link = null;
		if (shPost.getShPostType().getName().equals(ShSystemPostType.FILE.toString())) {
			link = shStaticFileUtils.getFileSourceBase() + "/" + shFolderUtils.getSite(shFolder).getName()
					+ shFolderUtils.folderPath(shFolder, false) + shPost.getTitle();
		} else {
			link = shFolderUtils.generateFolderLink(shFolder);
			link = link + shPost.getFurl();
		}
		return link;
	}

	public String generatePostLinkById(String postID) {
		if (postID != null) {
			try {
				ShPost shPost = shPostRepository.findById(postID).get();
				return this.generatePostLink(shPost);

			} catch (IllegalArgumentException exception) {
				return null;
			}
		} else {
			return null;
		}
	}

	public void referencedFile(ShPostAttr shPostAttr, ShPost shPost) {
		if (!shPost.getShPostType().getName().equals(ShSystemPostType.FILE.toString())) {
			this.referencedPost(shPostAttr, shPost);
		}
	}

	@Transactional
	public void referencedPost(ShPostAttr shPostAttr, ShPost shPost) {
		if (shPostAttr.getStrValue() == null) {
			shPostAttr.setReferenceObjects(null);
		} else {
			// TODO Two or more attributes with FILE Widget and same file, it cannot remove
			// a valid reference
			// Remove old references
			List<ShReference> shOldReferences = shReferenceRepository.findByShObjectFrom(shPost);
			if (shOldReferences.size() > 0) {
				for (ShReference shOldReference : shOldReferences) {
					// Find by shPostAttr.getStrValue()
					if (shOldReference.getShObjectTo().getId().toString().equals(shPostAttr.getStrValue())) {
						shReferenceRepository.delete(shOldReference);

					}

					// Find by shPostAttr.getReferenceObjects()
					if (shPostAttr.getReferenceObjects() != null) {
						for (ShObject shObject : shPostAttr.getReferenceObjects()) {
							if (shOldReference.getShObjectTo().getId().toString().equals(shObject.getId().toString())) {
								shReferenceRepository.delete(shOldReference);
							}
						}
					}
				}

			}

			try {
				ShObject shObjectReferenced = shObjectRepository.findById(shPostAttr.getStrValue()).get();
				// Create new reference
				ShReference shReference = new ShReference();
				shReference.setShObjectFrom(shPost);
				shReference.setShObjectTo(shObjectReferenced);
				shReferenceRepository.saveAndFlush(shReference);
				Set<ShObject> referenceObjects = new HashSet<ShObject>();
				referenceObjects.add(shObjectReferenced);
				shPostAttr.setReferenceObjects(referenceObjects);
			} catch (IllegalArgumentException iae) {
				// TODO Re-thing about ignore this
				shPostAttr.setReferenceObjects(null);
			}
		}
	}

	public void referencedObject(ShPostAttr shPostAttr, ShPost shPost) {
		if (shPostAttr.getShPostTypeAttr().getShWidget() != null) {
			if (shPostAttr.getShPostTypeAttr().getShWidget().getName().equals(ShSystemWidget.FILE)) {
				this.referencedFile(shPostAttr, shPost);
			} else if (shPostAttr.getShPostTypeAttr().getShWidget().getName().equals(ShSystemWidget.CONTENT_SELECT)) {
				this.referencedPost(shPostAttr, shPost);
			}
		}
	}

	public void referencedObject(ShPostAttr shPostAttrEdit, ShPostAttr shPostAttr, ShPost shPost) {
		if (shPostAttr.getShPostTypeAttr().getShWidget() != null) {
			if (shPostAttr.getShPostTypeAttr().getShWidget().getName().equals(ShSystemWidget.FILE)) {
				this.referencedFile(shPostAttrEdit, shPostAttr, shPost);
			} else if (shPostAttr.getShPostTypeAttr().getShWidget().getName().equals(ShSystemWidget.CONTENT_SELECT)) {
				this.referencedPost(shPostAttrEdit, shPostAttr, shPost);
			}
		}
	}

	public void referencedFile(ShPostAttr shPostAttrEdit, ShPostAttr shPostAttr, ShPost shPost) {
		if (shPost.getShPostType().getName().equals(ShSystemPostType.FILE)) {
			File fileFrom = shStaticFileUtils.filePath(shPost.getShFolder(), shPostAttrEdit.getStrValue());
			File fileTo = shStaticFileUtils.filePath(shPost.getShFolder(), shPostAttr.getStrValue());
			if (fileFrom != null && fileTo != null) {
				if (fileFrom.exists()) {
					if (!fileFrom.renameTo(fileTo)) {
						logger.error(String.format("Can't rename the file %s", fileFrom.getName()));
					}
				}
			}

		} else {
			this.referencedPost(shPostAttrEdit, shPostAttr, shPost);
		}

	}

	@Transactional
	public void referencedPost(ShPostAttr shPostAttrEdit, ShPostAttr shPostAttr, ShPost shPost) {
		if (shPostAttr.getStrValue() == null) {
			shPostAttr.setReferenceObjects(null);
		} else {
			// TODO Two or more attributes with FILE Widget and same file, it cannot remove
			// a valid reference
			// Remove old references
			List<ShReference> shOldReferences = shReferenceRepository.findByShObjectFrom(shPost);
			if (shOldReferences.size() > 0) {
				for (ShReference shOldReference : shOldReferences) {
					if (shPostAttrEdit.getReferenceObjects() != null) {
						for (ShObject shObject : shPostAttrEdit.getReferenceObjects()) {
							if (shOldReference.getShObjectTo().getId().toString().equals(shObject.getId().toString())) {
								shReferenceRepository.delete(shOldReference);
								break;
							}
						}
					}
				}
			}
			try {
				ShObject shObjectReferenced = shObjectRepository.findById(shPostAttr.getStrValue()).get();

				ShReference shReference = new ShReference();
				shReference.setShObjectFrom(shPost);
				shReference.setShObjectTo(shObjectReferenced);
				shReferenceRepository.saveAndFlush(shReference);

				Set<ShObject> referenceObjects = new HashSet<ShObject>();
				referenceObjects.add(shObjectReferenced);
				shPostAttr.setReferenceObjects(referenceObjects);
			} catch (IllegalArgumentException iae) {
				// TODO Re-thing about ignore this
				shPostAttr.setReferenceObjects(null);
			}
		}
	}

	public ShSite getSite(ShPost shPost) {
		return shFolderUtils.getSite(shPost.getShFolder());
	}

}
