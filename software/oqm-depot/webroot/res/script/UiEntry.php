<?php

namespace Ebprod\OqmCoreDepot;
class UiEntry {
	private EntryCategory $type;
	private string $url;
	private string $name;
	private string $description;
	
	/**
	 * @param string $type
	 */
	public function __construct(string $fileLoc) {
		$json = array();
		{
			$jsonRaw = file_get_contents($fileLoc);
			//json_validate($jsonRaw);//needed?
			$json = json_decode($jsonRaw, true);
		}
		
		$this->type = EntryCategory::fromName($json['type']);
		$this->url = htmlspecialchars($json['url']);
		$this->name = htmlspecialchars($json['name']);
		$this->description = htmlspecialchars($json['description']);
		
	}
	
	public function getType(): EntryCategory {
		return $this->type;
	}
	
	public function getUrl(): string {
		return $this->url;
	}
	
	public function getName(): string {
		return $this->name;
	}
	
	public function getDescription(): string {
		return $this->description;
	}
}