package tech.ebp.oqm.core.api.service.mongo.image;

import com.mongodb.client.ClientSession;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.config.ImageResizeConfig;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.rest.media.ImageGet;
import tech.ebp.oqm.core.api.model.rest.media.file.FileUploadBody;
import tech.ebp.oqm.core.api.model.rest.search.ImageSearch;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.ItemCategoryService;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;
import tech.ebp.oqm.core.api.service.mongo.file.MongoHistoriedFileService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Slf4j
@ApplicationScoped
public class ImageService extends MongoHistoriedFileService<Image, FileUploadBody, ImageSearch, ImageGet> {

	@Inject
	ImageResizeConfig imageResizeConfig;
	@Inject
	StorageBlockService storageBlockService;
	@Inject
	ItemCategoryService itemCategoryService;
	@Inject
	InventoryItemService inventoryItemService;
	
	public ImageService() {
		super(
			Image.class,
			"image",
			false
		);
		this.allowedMimeTypes = Set.of(
			"image/png",
			"image/jpeg",
			"image/bmp",
			"image/tiff",
			"image/webp",
			"image/gif"
		);
	}
	
	/**
	 * Resizes the given image to what should be held in the object.
	 *
	 * @param inputImage The image to resize
	 *
	 * @return The resized image
	 */
	public BufferedImage resizeImage(BufferedImage inputImage) {
		log.debug("Resizing image {}", inputImage);
		// creates output image
		BufferedImage outputImage = new BufferedImage(
			this.imageResizeConfig.width(),
			this.imageResizeConfig.height(),
			inputImage.getType()
		);
		
		// scales the input image to the output image
		Graphics2D g2d = outputImage.createGraphics();
		g2d.drawImage(
			inputImage,
			0,
			0,
			this.imageResizeConfig.width(),
			this.imageResizeConfig.height(),
			null
		);
		g2d.dispose();
		
		return outputImage;
	}
	
	@Override
	public ObjectId add(String oqmDbIdOrName, ClientSession clientSession, Image fileObject, File origImage, String fileName, InteractingEntity interactingEntity) throws IOException {
		File usingImage;
		if(this.imageResizeConfig.enabled()) {
			usingImage = this.getTempFileService().getTempFile(
				FilenameUtils.removeExtension(fileName) + "-resized",
				FilenameUtils.getExtension(fileName),
				"imageUploads"
			);
			log.info("Image needs resized: {}", origImage);
			{
				BufferedImage bufferedImage = ImageIO.read(origImage);

				if(bufferedImage == null){
					throw new IllegalArgumentException("Image data given was invalid or unsupported.");
				}

				bufferedImage = resizeImage(bufferedImage);
				ImageIO.write(bufferedImage, this.imageResizeConfig.savedType(), usingImage);
			}
		} else {
			usingImage = origImage;
		}
		
		return super.add(oqmDbIdOrName, clientSession, fileObject, usingImage, fileName, interactingEntity);
	}
	
	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return super.addBaseStats(oqmDbIdOrName, CollectionStats.builder())
				   .build();
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(String oqmDbIdOrName, boolean newObject, Image newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(oqmDbIdOrName, newObject, newOrChangedObject, clientSession);
	}
	
	@Override
	public ImageGet fileObjToGet(String oqmDbIdOrName, Image obj) {
		return ImageGet.fromImage(obj, this.getRevisions(oqmDbIdOrName, obj.getId()));
	}
	
	@WithSpan
	@Override
	public Map<String, Set<ObjectId>> getReferencingObjects(String oqmDbIdOrName, ClientSession cs, Image objectToRemove) {
		Map<String, Set<ObjectId>> objsWithRefs = super.getReferencingObjects(oqmDbIdOrName, cs, objectToRemove);
		
		Set<ObjectId> refs = this.storageBlockService.getBlocksReferencing(oqmDbIdOrName, cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.storageBlockService.getClazz().getSimpleName(), refs);
		}
		refs = this.inventoryItemService.getItemsReferencing(oqmDbIdOrName, cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.inventoryItemService.getClazz().getSimpleName(), refs);
		}
		refs = this.itemCategoryService.getItemCatsReferencing(oqmDbIdOrName, cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.itemCategoryService.getClazz().getSimpleName(), refs);
		}
		
		return objsWithRefs;
	}
}
