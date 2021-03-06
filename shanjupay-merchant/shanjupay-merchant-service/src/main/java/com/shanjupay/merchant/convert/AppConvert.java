package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.entity.App;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author FreeLoop
 * @date 2022/5/14 17:29
 */
@Mapper
public interface AppConvert {

    AppConvert INSTANCE = Mappers.getMapper(AppConvert.class);
    AppDTO entity2dto(App entity);
    App dto2entity(AppDTO dto);
    List<AppDTO> listentity2dto(List<App> app);

}
