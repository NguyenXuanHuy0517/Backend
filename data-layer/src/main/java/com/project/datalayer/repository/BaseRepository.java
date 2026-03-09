package com.project.datalayer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.io.Serializable;

/**
 * Interface BaseRepository dùng chung cho toàn bộ hệ thống.
 * Ngăn Spring Data JPA tạo instance cho Interface này bằng @NoRepositoryBean.
 * @param <T>: Kiểu dữ liệu của Entity (User, Room, Contract...)
 * @param <ID>: Kiểu dữ liệu của khóa chính (thường là Long hoặc Integer)
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
}
