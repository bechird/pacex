
update job j, order_T ot
 set j.due_date = ot.due_date 
 where j.order_id = ot.order_id; 
 
 update job j, part ot
 set j.part_color = ot.colors
 where j.Part_Num = ot.Part_Num; 
 
 update job j, part ot
 set j.part_isbn = ot.isbn
 where j.Part_Num = ot.Part_Num; 
 
 update job j, part ot
 set j.part_paper_id = ot.Paper_Type
 where j.Part_Num = ot.Part_Num; 
 
 update job j, part ot
 set j.part_title = ot.Title
 where j.Part_Num = ot.Part_Num; 
 
 update job j, order_T ot
 set j.Production_Mode = ot.Production_Mode
 where j.Order_Id = ot.Order_Id; 
 
 update job j, part ot
 set j.Part_Category = ot.Category_Id
 where j.part_Num = ot.Part_Num; 