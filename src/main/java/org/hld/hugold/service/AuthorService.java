package org.hld.hugold.service;

import org.hld.hugold.dao.AuthorRepository;
import org.hld.hugold.entity.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorService {

	@Autowired
	private AuthorRepository repository;

	public Author addAuthorIfNotExist(String authorName) {
		return repository.findByName(authorName).stream().findAny().orElseGet(() -> {
			final Author newAuthor = new Author();
			newAuthor.setName(authorName);
			return repository.save(newAuthor);
		});
	}

	public long deleteAuthor(String id) {
		if (repository.findById(id).isPresent()) {
			repository.deleteById(id);
			return 1;
		} else {
			return 0;
		}
	}
}