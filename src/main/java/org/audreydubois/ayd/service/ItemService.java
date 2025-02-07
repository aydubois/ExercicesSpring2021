package org.audreydubois.ayd.service;
import io.swagger.models.HttpMethod;
import org.audreydubois.ayd.dao.RegionRepository;
import org.audreydubois.ayd.dto.ItemDTO;
import org.audreydubois.ayd.dto.RegionDTO;
import org.audreydubois.ayd.entity.Item;
import org.audreydubois.ayd.entity.Region;
import org.audreydubois.ayd.exception.ItemNotFoundException;
import org.audreydubois.ayd.dao.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.spec.PSource;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final RegionRepository regionRepository;
    private final RestTemplate restTemplate;

    public ItemService(ItemRepository repository, RegionRepository regionRepository, RestTemplate restTemplate){
        this.itemRepository = repository;
        this.restTemplate = restTemplate;
        this.regionRepository = regionRepository;
    }
    public ResponseEntity<List<Item>> getAll(){
        List<Item> items = itemRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(items);
    }

    public ResponseEntity<Item> get(Long id) throws ItemNotFoundException{
        Item item = itemRepository.findById(id).orElseThrow(()->new ItemNotFoundException(id));
        return ResponseEntity.status(HttpStatus.OK).body(item);
    }

    public ResponseEntity<Item> add(ItemDTO item) throws ItemNotFoundException{
        if(item == null){
            throw new ItemNotFoundException(null);
        }
        Item newItem = new Item(item.getName(), item.getRegionCode(), this.findRegion(item.getRegionCode()));
        Item itemSave = itemRepository.save(newItem);
        return ResponseEntity.status(HttpStatus.OK).body(itemSave);
    }

    public ResponseEntity<String> delete(Long id){
        itemRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Item n°"+id+" deleted");
    }

    public ResponseEntity<Item> patch(ItemDTO newItem, Long id) throws ItemNotFoundException{
        if(newItem == null){
            throw new ItemNotFoundException(id);
        }
        Optional<Object> newItemSave =  itemRepository.findById(id).map(
            item -> {
                item.setName(newItem.getName());
                item.setRegionCode(newItem.getRegionCode());
                item.setRegion(this.findRegion(newItem.getRegionCode()));
                return itemRepository.save(item);
            }
        );
        if(newItemSave.isEmpty()){
            Item newI = new Item(newItem.getName(), newItem.getRegionCode(), this.findRegion(newItem.getRegionCode()));
            Item itemSave = itemRepository.save(newI);
            return ResponseEntity.status(HttpStatus.OK).body(itemSave);
        }
        return ResponseEntity.status(HttpStatus.OK).body((Item)newItemSave.get());
    }
    public ResponseEntity<Item> patchItem(Item newItem, Long id) throws ItemNotFoundException{
        if(newItem == null){
            throw new ItemNotFoundException(id);
        }
        Optional<Object> newItemSave =  itemRepository.findById(id).map(
                item -> {
                    item.setName(newItem.getName());
                    item.setRegionCode(newItem.getRegionCode());
                    item.setRegion(newItem.getRegion());
                    return itemRepository.save(item);
                }
        );
        if(newItemSave.isEmpty()){
            Item newI = new Item(newItem.getName(), newItem.getRegionCode(), newItem.getRegion());
            Item itemSave = itemRepository.save(newI);
            return ResponseEntity.status(HttpStatus.OK).body(itemSave);
        }
        return ResponseEntity.status(HttpStatus.OK).body((Item)newItemSave.get());
    }


    public String findRegion(String regionCode){
        try{

         ResponseEntity<RegionDTO> result = restTemplate.getForEntity(
                "https://geo.api.gouv.fr/regions/" + regionCode, RegionDTO.class);
            return result.getBody() != null ? result.getBody().getNom() : null;
        }catch(RestClientException e){
            return null;
        }
    }

    public RegionDTO[] findAllRegions(){
        try{
            ResponseEntity<RegionDTO[]> result = restTemplate.getForEntity(
                    "https://geo.api.gouv.fr/regions", RegionDTO[].class);
            System.out.println(result);
            return result.getBody() != null ? result.getBody() : null;
        }catch(RestClientException e){
            return null;
        }
    }

    public List<Region> findRegionDB(String regionCode){
        return regionRepository.findByRegionCode(regionCode);
    }
    public List<Region> findAllRegionsDB(){
        return regionRepository.findAll();
    }
}
