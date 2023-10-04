const ItemStoredAddSubTransfer = {
	//TODO:: move things here
	formMessages: $("#itemStoredAddSubTransFormMessages"),
	form: $("#itemStoredAddSubTransForm"),
	formItemImg: $("#itemStoredAddSubTransFormItemImg"),
	formItemNameLabel: $("#itemStoredAddSubTransFormItemNameLabel"),
	toFromExistingStoredCheckbox: $("#itemStoredAddSubTransFormToFromExistingStoredCheckbox"),
	toSelect: $("#itemStoredAddSubTransFormToSelect"),
	fromSelect: $("#itemStoredAddSubTransFormFromSelect"),

	resetForms(){
		this.formItemImg.attr("src", "");
		this.formMessages.text("");
		this.formItemNameLabel.text("");
	},

	disableExistingStoredOption(){
		this.toFromExistingStoredCheckbox.prop('checked', false);
		this.toFromExistingStoredCheckbox.disable();
	},
	enableExistingStoredOption(){
		this.toFromExistingStoredCheckbox.enable();
	},

	setupForItem(itemId){
		this.resetForms();
		this.formItemImg.attr("src", "/api/v1/media/image/for/item/" + itemId);
		doRestCall({
			spinnerContainer: null,
			url: "/api/v1/inventory/item/" + itemId,
			done: function (data) {
				ItemStoredAddSubTransfer.formItemNameLabel.text(data.name);
				let storageBLockIds = Object.keys(data.storageMap);
				console.log("Storage block ids: " + storageBLockIds);
				//TODO:: check for no block ids

				ItemStoredAddSubTransfer.setupFromToSelects(storageBLockIds);

				StoredTypeUtils.foreachStoredType(
					data.storageType,
					function (){
						ItemStoredAddSubTransfer.disableExistingStoredOption();
						//TODO
					},
					function (){
						ItemStoredAddSubTransfer.enableExistingStoredOption();
						//TODO
					},
					function (){
						ItemStoredAddSubTransfer.disableExistingStoredOption();
						//TODO
					}
				);
			}
		});
	},

	setupFromToSelects(storageBlockIds, allowSelectSame = false){
		ItemStoredAddSubTransfer.fromSelect.text("");
		ItemStoredAddSubTransfer.toSelect.text("");

		storageBlockIds.forEach(function(curStorageBlockId){
			getStorageBlockLabel(curStorageBlockId, function (blockLabel){
				let newOptionTo = $('<option></option>');
				newOptionTo.attr("id", curStorageBlockId);
				newOptionTo.text(blockLabel);

				let newOptionFrom = newOptionTo.clone(true, true);

				ItemStoredAddSubTransfer.fromSelect.append(newOptionFrom);
				ItemStoredAddSubTransfer.toSelect.append(newOptionTo);
			});

			if(!allowSelectSame){
				//TODO:: this
				//TODO:: check if only one block associated
			}
		});
	}


};