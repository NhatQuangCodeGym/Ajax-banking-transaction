package com.cg.controller.api;

import com.cg.exception.DataInputException;
import com.cg.model.Customer;
import com.cg.model.Deposit;
import com.cg.model.Withdraw;
import com.cg.service.CustomerService;
import com.cg.utils.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerRestController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AppUtil appUtil;

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        Optional<Customer> customer = customerService.findById(id);

        if (customer.isPresent()) {
            return new ResponseEntity<>(customer.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        }
    }


    @PostMapping("/create")
    public ResponseEntity<Customer> doCreate(@RequestBody Customer customer) {
        customer.setId(0L);
        Customer createdCustomer = customerService.save(customer);

        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<Customer> doUpdate(@RequestBody Customer customer) {
        Long id = customer.getId();
        customerService.save(customer);
        Customer updatedCustomer = customerService.findById(id).get();

        return new ResponseEntity<>(updatedCustomer, HttpStatus.CREATED);
    }

    @PutMapping("/deposit/{id}")
    public ResponseEntity<?> doDeposit(@PathVariable Long id, @Validated @RequestBody Deposit deposit, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return appUtil.mapErrorToResponse(bindingResult);
        }
        Optional<Customer> optionalCustomer = customerService.findById(id);
        if(optionalCustomer.isPresent()) {
            BigDecimal currentBalance = optionalCustomer.get().getBalance();
            BigDecimal transactionAmount = deposit.getTransactionAmount();
            customerService.incrementBalance(id, deposit);
            Customer updatedCustomer = customerService.findById(id).get();
            return new ResponseEntity<>(updatedCustomer, HttpStatus.CREATED);
        } else{
            throw new DataInputException("Customer's information not valid");

        }
//        customerService.incrementBalance(id, deposit);
//        Customer updatedCustomer = customerService.findById(id).get();
//        return new ResponseEntity<>(updatedCustomer, HttpStatus.CREATED);

    }

    @PutMapping("/withdraw/{id}")
    public ResponseEntity<?> doWithdraw(@PathVariable Long id, @Validated @RequestBody Withdraw withdraw, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return appUtil.mapErrorToResponse(bindingResult);
        }

        Optional<Customer> optionalCustomer = customerService.findById(id);

        if (optionalCustomer.isPresent()) {
            BigDecimal currentBalance = optionalCustomer.get().getBalance();
            BigDecimal transactionAmount = withdraw.getTransactionAmount();
            if (currentBalance.compareTo(transactionAmount) >= 0) {
                customerService.reduceBalance(id, withdraw);
                Customer updatedCustomer = customerService.findById(id).get();
                return new ResponseEntity<>(updatedCustomer, HttpStatus.CREATED);

            } else {
                throw new DataInputException("Not enough to withdraw");
            }
        } else {
            throw new DataInputException("Customer's information not valid");
        }
    }
}
