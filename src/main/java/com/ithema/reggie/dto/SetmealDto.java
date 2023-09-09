package com.ithema.reggie.dto;


import com.ithema.reggie.entity.Setmeal;
import com.ithema.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
