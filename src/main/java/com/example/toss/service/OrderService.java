package com.example.toss.service;

import com.example.toss.dto.ItemOrderDto;
import com.example.toss.dto.PaymentConfirmDto;
import com.example.toss.entity.Item;
import com.example.toss.entity.ItemOrder;
import com.example.toss.repo.ItemRepository;
import com.example.toss.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final TossHttpService tossService;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    public Object confirmPayment(PaymentConfirmDto dto) {
        // HTTP 요청이 보내진다.
        Object tossPaymentObj = tossService.confirmPayment(dto);
        log.info(tossPaymentObj.toString());
        // todo: 사용자가 결제한 물품 + 결제 정보에 대한 내용을 DB에 저장
        // 1. 결제한 물품 정보를 응답 Body에서 찾는다 ( orderName )
        String orderName = ((LinkedHashMap<String, Object>) tossPaymentObj)
                .get("orderName").toString();
        // 2. orderName에서 itemId를 회수하고, 그에 해당하는 Item 엔티티를 조회한다.
        Long itemId = Long.parseLong(orderName.split("-")[0]);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        // 3. Item 엔티티를 바탕으로 ItemOrder를 만들자.
        return ItemOrderDto.fromEntity(orderRepository.save(ItemOrder.builder()
                .item(item)
                .tossPaymentKey(dto.getPaymentKey())
                .tossOrderId(dto.getOrderId())
                .status("DONE")
                .build()));

        //return tossPaymentObj;

    }

    // readAll
    public List<ItemOrderDto> readAll() {
        return orderRepository.findAll().stream()
                .map(ItemOrderDto::fromEntity)
                .toList();
    }
    // readOne
    public ItemOrderDto readOne(Long id) {
        return orderRepository.findById(id)
                .map(ItemOrderDto::fromEntity)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // readTossPayment
}
